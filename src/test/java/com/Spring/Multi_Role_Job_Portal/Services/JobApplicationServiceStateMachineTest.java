package com.Spring.Multi_Role_Job_Portal.Services;

import com.Spring.Multi_Role_Job_Portal.Config.ApplicationProperties;
import com.Spring.Multi_Role_Job_Portal.Entities.ApplicationStatusHistory;
import com.Spring.Multi_Role_Job_Portal.Entities.CandidateProfile;
import com.Spring.Multi_Role_Job_Portal.Entities.Job;
import com.Spring.Multi_Role_Job_Portal.Entities.JobApplication;
import com.Spring.Multi_Role_Job_Portal.Entities.RecruiterProfile;
import com.Spring.Multi_Role_Job_Portal.Entities.User;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.JobStatus;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.RoleType;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.StatusType;
import com.Spring.Multi_Role_Job_Portal.Repositories.ApplicationStatusHistoryRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.CandidateProfileRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.JobApplicationRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.JobRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.RecruiterProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceStateMachineTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private ApplicationStatusHistoryRepository applicationStatusHistoryRepository;
    @Mock
    private CandidateProfileRepository candidateProfileRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private RecruiterProfileRepository recruiterProfileRepository;
    @Mock
    private MatchingService matchingService;
    @Mock
    private ApplicationProperties applicationProperties;
    @InjectMocks
    private JobApplicationService jobApplicationService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void scoresApplicationThroughStateMachineAndWritesHistory() throws Exception {
        User recruiter = user(1L, RoleType.RECRUITER);
        JobApplication application = recruiterApplication(StatusType.APPLIED, recruiter);
        authenticate(recruiter);

        when(jobApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(recruiterProfileRepository.findByUser(recruiter))
                .thenReturn(Optional.of(application.getJob().getCreatedBy()));
        when(matchingService.computeMatchScore(application.getJob(), application.getCandidateProfile())).thenReturn(80.0);

        jobApplicationService.shortList(application.getId());

        assertEquals(StatusType.SUGGESTED, application.getStatus());
        ArgumentCaptor<ApplicationStatusHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationStatusHistory.class);
        verify(applicationStatusHistoryRepository).save(historyCaptor.capture());
        ApplicationStatusHistory history = historyCaptor.getValue();
        assertEquals(StatusType.APPLIED, history.getFromStatus());
        assertEquals(StatusType.SUGGESTED, history.getToStatus());
        assertEquals(recruiter, history.getChangedBy());
        assertNotNull(history.getChangedAt());
    }

    @Test
    void rejectsInvalidTransitionFromAppliedToHired() throws Exception {
        User recruiter = user(1L, RoleType.RECRUITER);
        JobApplication application = recruiterApplication(StatusType.APPLIED, recruiter);
        authenticate(recruiter);

        when(jobApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(recruiterProfileRepository.findByUser(recruiter))
                .thenReturn(Optional.of(application.getJob().getCreatedBy()));

        assertThrows(IllegalStateException.class, () -> jobApplicationService.hireCandidate(application.getId()));
        verify(applicationStatusHistoryRepository, never()).save(any());
    }

    @Test
    void candidateCanWithdrawOwnActiveApplicationAndAuditIsRecorded() throws Exception {
        User candidateUser = user(2L, RoleType.CANDIDATE);
        CandidateProfile candidate = CandidateProfile.builder().id(20L).user(candidateUser).build();
        JobApplication application = JobApplication.builder()
                .id(30L)
                .job(Job.builder().title("Backend Developer").status(JobStatus.OPEN).build())
                .candidateProfile(candidate)
                .status(StatusType.SUGGESTED)
                .build();
        authenticate(candidateUser);

        when(candidateProfileRepository.findByUser(candidateUser)).thenReturn(Optional.of(candidate));
        when(jobApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        jobApplicationService.withdraw(application.getId());

        assertEquals(StatusType.WITHDRAWN, application.getStatus());
        ArgumentCaptor<ApplicationStatusHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationStatusHistory.class);
        verify(applicationStatusHistoryRepository).save(historyCaptor.capture());
        assertEquals(StatusType.WITHDRAWN, historyCaptor.getValue().getToStatus());
        assertEquals(candidateUser, historyCaptor.getValue().getChangedBy());
    }

    @Test
    void blocksReapplicationDuringRejectionCooldown() throws Exception {
        User candidateUser = user(2L, RoleType.CANDIDATE);
        CandidateProfile candidate = CandidateProfile.builder().id(20L).user(candidateUser).build();
        Job job = Job.builder().id(10L).title("Backend Developer").status(JobStatus.OPEN).build();
        authenticate(candidateUser);

        when(candidateProfileRepository.findByUser(candidateUser)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(applicationProperties.getRejectionCooldownDays()).thenReturn(30);
        when(jobApplicationRepository.existsByJobAndCandidateProfileAndStatusIn(eq(job), eq(candidate), any()))
                .thenReturn(false);
        when(jobApplicationRepository.existsByJobAndCandidateProfileAndStatusAndUpdatedAtAfter(
                eq(job), eq(candidate), eq(StatusType.REJECTED), any()))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> jobApplicationService.applyToJobById(job.getId()));
        verify(jobApplicationRepository, never()).save(any(JobApplication.class));
    }

    @Test
    void allowsReapplicationAfterCooldownAndAuditsTheNewApplication() throws Exception {
        User candidateUser = user(2L, RoleType.CANDIDATE);
        CandidateProfile candidate = CandidateProfile.builder().id(20L).user(candidateUser).build();
        Job job = Job.builder().id(10L).title("Backend Developer").status(JobStatus.OPEN).build();
        authenticate(candidateUser);

        when(candidateProfileRepository.findByUser(candidateUser)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(applicationProperties.getRejectionCooldownDays()).thenReturn(30);
        when(jobApplicationRepository.existsByJobAndCandidateProfileAndStatusIn(eq(job), eq(candidate), any()))
                .thenReturn(false);
        when(jobApplicationRepository.existsByJobAndCandidateProfileAndStatusAndUpdatedAtAfter(
                eq(job), eq(candidate), eq(StatusType.REJECTED), any()))
                .thenReturn(false);

        jobApplicationService.applyToJobById(job.getId());

        ArgumentCaptor<ApplicationStatusHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationStatusHistory.class);
        verify(applicationStatusHistoryRepository).save(historyCaptor.capture());
        assertNull(historyCaptor.getValue().getFromStatus());
        assertEquals(StatusType.APPLIED, historyCaptor.getValue().getToStatus());
    }

    private JobApplication recruiterApplication(StatusType status, User recruiterUser) {
        RecruiterProfile recruiter = RecruiterProfile.builder().id(10L).user(recruiterUser).build();
        CandidateProfile candidate = CandidateProfile.builder().id(20L).build();
        Job job = Job.builder()
                .id(40L)
                .title("Backend Developer")
                .status(JobStatus.OPEN)
                .minMatchScore(70.0)
                .createdBy(recruiter)
                .build();
        return JobApplication.builder()
                .id(30L)
                .job(job)
                .candidateProfile(candidate)
                .status(status)
                .build();
    }

    private User user(Long id, RoleType role) {
        return User.builder()
                .id(id)
                .email(role.name().toLowerCase() + "@example.com")
                .role(role)
                .enabled(true)
                .build();
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );
    }
}
