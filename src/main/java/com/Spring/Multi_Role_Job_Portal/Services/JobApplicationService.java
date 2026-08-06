package com.Spring.Multi_Role_Job_Portal.Services;

import com.Spring.Multi_Role_Job_Portal.Config.ApplicationProperties;
import com.Spring.Multi_Role_Job_Portal.DTO.ApplicationStatusHistoryResponseDTO;
import com.Spring.Multi_Role_Job_Portal.DTO.JobApplicationResponseDTO;
import com.Spring.Multi_Role_Job_Portal.Entities.*;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.JobStatus;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.RoleType;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.StatusType;
import com.Spring.Multi_Role_Job_Portal.Repositories.CandidateProfileRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.ApplicationStatusHistoryRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.JobApplicationRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.JobRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.RecruiterProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private static final Map<StatusType, Set<StatusType>> VALID_TRANSITIONS = Map.of(
            StatusType.APPLIED, EnumSet.of(StatusType.SUGGESTED, StatusType.SHORTLISTED, StatusType.REJECTED, StatusType.WITHDRAWN),
            StatusType.SUGGESTED, EnumSet.of(StatusType.SHORTLISTED, StatusType.REJECTED, StatusType.WITHDRAWN),
            StatusType.SHORTLISTED, EnumSet.of(StatusType.HIRED, StatusType.REJECTED, StatusType.WITHDRAWN),
            StatusType.HIRED, EnumSet.noneOf(StatusType.class),
            StatusType.REJECTED, EnumSet.noneOf(StatusType.class),
            StatusType.WITHDRAWN, EnumSet.noneOf(StatusType.class)
    );

    private static final Set<StatusType> REAPPLICATION_BLOCKING_STATUSES = EnumSet.of(
            StatusType.APPLIED,
            StatusType.SUGGESTED,
            StatusType.SHORTLISTED,
            StatusType.HIRED,
            StatusType.WITHDRAWN
    );

    private final JobApplicationRepository jobApplicationRepository;
    private final ApplicationStatusHistoryRepository applicationStatusHistoryRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobRepository jobRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final MatchingService matchingService;
    private final ApplicationProperties applicationProperties;

    @Transactional
    public JobApplicationResponseDTO applyToJobById(Long jobId) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //Job is only applied by candidate
        if(user==null || user.getRole()!= RoleType.CANDIDATE)
            throw new AccessDeniedException("User is not valid to apply to a job");

        CandidateProfile candidateProfile = candidateProfileRepository.findByUser(user).orElseThrow(() -> new EntityNotFoundException("Candidate profile not found"));

        Job job = jobRepository.findById(jobId).orElseThrow(() -> new EntityNotFoundException("Job couldnt be found"));

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalStateException("This job is closed and no longer accepting applications");
        }

        boolean hasBlockingApplication = jobApplicationRepository.existsByJobAndCandidateProfileAndStatusIn(
                job,
                candidateProfile,
                REAPPLICATION_BLOCKING_STATUSES
        );
        if (hasBlockingApplication) {
            throw new DataIntegrityViolationException("You have already applied to this job");
        }

        LocalDateTime cooldownStart = LocalDateTime.now()
                .minusDays(applicationProperties.getRejectionCooldownDays());
        boolean rejectedDuringCooldown = jobApplicationRepository
                .existsByJobAndCandidateProfileAndStatusAndUpdatedAtAfter(
                        job,
                        candidateProfile,
                        StatusType.REJECTED,
                        cooldownStart
                );
        if (rejectedDuringCooldown) {
            throw new IllegalStateException(
                    "You can reapply " + applicationProperties.getRejectionCooldownDays()
                            + " days after a rejected application"
            );
        }

        JobApplication jobApplication = JobApplication.builder()
                .job(job)
                .candidateProfile(candidateProfile)
                .build();

        jobApplicationRepository.save(jobApplication);
        transition(jobApplication, StatusType.APPLIED, user, "Application submitted");

        return toResponse(jobApplication);

    }


    public List<JobApplicationResponseDTO> getAllApplications() throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //Job is only applied by candidate
        //We dont need this cause we check the role in the Security Filter Chain only ---
//        if(user==null || user.getRole()!= RoleType.CANDIDATE)
//            throw new AccessDeniedException("User is not valid to see job applications in this way");

        CandidateProfile candidateProfile = candidateProfileRepository.findByUser(user).orElseThrow(() -> new EntityNotFoundException("Candidate profile not found"));

        List<JobApplication> jobApplications = jobApplicationRepository.findAllByCandidateProfile(candidateProfile);

        return jobApplications.stream()
                .map(this::toResponse)
                .toList();
    }


    public List<JobApplicationResponseDTO> getAllApplicationsForRecruiter(Long jobId) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //Job is only applied by candidate
        //We dont need this cause we check the role in the Security Filter Chain only ---
//        if(user==null || user.getRole()!= RoleType.RECRUITER)
//            throw new AccessDeniedException("User is not valid to see job applications in this way");

        RecruiterProfile recruiterProfile = recruiterProfileRepository.findByUser(user).orElseThrow(() -> new EntityNotFoundException("Recruiter Profile not found"));

        Job job = jobRepository.findById(jobId).orElseThrow(() -> new EntityNotFoundException("Job not found"));
        //Checking if the recruiter of the job given to us and the user recruiter are the same
        if (!job.getCreatedBy().getId().equals(recruiterProfile.getId()))
            throw new AccessDeniedException("You are not allowed to view applications for this job");

        List<JobApplication> jobApplications = jobApplicationRepository.findAllByJobOrderByMatchScoreDesc(job);

        return jobApplications.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public JobApplicationResponseDTO shortList(Long applicationId) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //Job is only checked by recruiter
        if(user==null || user.getRole()!= RoleType.RECRUITER)
            throw new AccessDeniedException("User is not valid to shortlist job applications");

        JobApplication jobApplication = jobApplicationRepository.findById(applicationId).orElseThrow(() -> new EntityNotFoundException("job Application not found"));

        RecruiterProfile recruiterProfile = recruiterProfileRepository.findByUser(user).orElseThrow(() -> new EntityNotFoundException("Recruiter Profile not found"));

        Job job = jobApplication.getJob();
        CandidateProfile candidateProfile = jobApplication.getCandidateProfile();

        //Checking if the recruiter of the job given to us and the user recruiter are the same
        if (!job.getCreatedBy().getId().equals(recruiterProfile.getId()))
            throw new AccessDeniedException("You are not allowed to view applications for this job");

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalStateException("Cannot shortlist for closed job");
        }

        double matchScore = matchingService.computeMatchScore(job, candidateProfile);
        jobApplication.setMatchScore(matchScore);
        double minMatchScore = job.getMinMatchScore() == null ? 70.0 : job.getMinMatchScore();
        transition(
                jobApplication,
                matchScore >= minMatchScore ? StatusType.SUGGESTED : StatusType.REJECTED,
                user,
                "Weighted match score: " + matchScore
        );

        return toResponse(jobApplication);
    }

    /**
     * Confirms a score-based suggestion after the recruiter has reviewed it.
     */
    @Transactional
    public JobApplicationResponseDTO confirmShortlist(Long applicationId) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null || user.getRole() != RoleType.RECRUITER) {
            throw new AccessDeniedException("User is not valid to shortlist job applications");
        }

        JobApplication jobApplication = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Job application not found"));
        RecruiterProfile recruiterProfile = recruiterProfileRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Recruiter Profile not found"));

        Job job = jobApplication.getJob();
        if (!job.getCreatedBy().getId().equals(recruiterProfile.getId())) {
            throw new AccessDeniedException("You are not allowed to shortlist this job application");
        }
        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalStateException("Cannot shortlist for closed job");
        }
        transition(jobApplication, StatusType.SHORTLISTED, user, "Shortlist confirmed by recruiter");
        return toResponse(jobApplication);
    }

    @Transactional
    public JobApplicationResponseDTO withdraw(Long applicationId) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null || user.getRole() != RoleType.CANDIDATE) {
            throw new AccessDeniedException("Only candidates can withdraw job applications");
        }

        CandidateProfile candidateProfile = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Candidate profile not found"));
        JobApplication jobApplication = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Job application not found"));

        if (!jobApplication.getCandidateProfile().getId().equals(candidateProfile.getId())) {
            throw new AccessDeniedException("You are not allowed to withdraw this job application");
        }

        transition(jobApplication, StatusType.WITHDRAWN, user, "Application withdrawn by candidate");
        return toResponse(jobApplication);
    }

    public List<JobApplicationResponseDTO> getAllShortListedApplications(Long jobId) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //Job is only checked by recruiter
        if(user==null || user.getRole()!= RoleType.RECRUITER)
            throw new AccessDeniedException("User is not valid to shortlist job applications");

        RecruiterProfile recruiterProfile = recruiterProfileRepository.findByUser(user).orElseThrow(() -> new EntityNotFoundException("Recruiter Profile not found"));

        Job job = jobRepository.findById(jobId).orElseThrow(()-> new EntityNotFoundException("Job couldnt be found"));

        //Checking if the recruiter of the job given to us and the user recruiter are the same
        if (!job.getCreatedBy().getId().equals(recruiterProfile.getId()))
            throw new AccessDeniedException("You are not allowed to view applications for this job");

        List<JobApplication> jobApplications = jobApplicationRepository.findAllByJobAndStatus(job,StatusType.SHORTLISTED);

        return jobApplications.stream()
                .map(this::toResponse)
                .toList();

    }


    @Transactional
    public JobApplicationResponseDTO hireCandidate(Long applicationId) throws AccessDeniedException {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (user.getRole() != RoleType.RECRUITER) {
                throw new AccessDeniedException("Only recruiters can close jobs");
            }

            RecruiterProfile recruiterProfile = recruiterProfileRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("Recruiter profile not found"));

            JobApplication selected = jobApplicationRepository.findById(applicationId)
                    .orElseThrow(() -> new EntityNotFoundException("Application not found"));

            Job job = selected.getJob();

            //Checking if the recruiter of the job given to us and the user recruiter are the same
            if (!job.getCreatedBy().getId().equals(recruiterProfile.getId()))
                throw new AccessDeniedException("You are not allowed to view applications for this job");

            if (job.getStatus() == JobStatus.CLOSED) {
                throw new IllegalStateException("This job is already closed");
            }

            // 1️⃣ Hire selected candidate
            transition(selected, StatusType.HIRED, user, "Candidate hired for this job");

            // 2️⃣ Reject all others
            List<JobApplication> all = jobApplicationRepository.findByJob(job);
            for (JobApplication app : all) {
                if (!app.getId().equals(applicationId) && !isTerminal(app.getStatus())) {
                    transition(app, StatusType.REJECTED, user, "Another candidate was hired for this job");
                }
            }

            // 3️⃣ Close the job
            job.setStatus(JobStatus.CLOSED);

            jobRepository.save(job);

            return toResponse(selected);
        }
        catch (OptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT , "Job was modified by another request");
        }
    }

    @Transactional(readOnly = true)
    public List<ApplicationStatusHistoryResponseDTO> getApplicationHistory(Long applicationId)
            throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JobApplication jobApplication = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Job application not found"));

        if (user == null || !canViewApplicationHistory(user, jobApplication)) {
            throw new AccessDeniedException("You are not allowed to view this application history");
        }

        return applicationStatusHistoryRepository
                .findAllByJobApplicationOrderByChangedAtAsc(jobApplication)
                .stream()
                .map(history -> ApplicationStatusHistoryResponseDTO.builder()
                        .fromStatus(history.getFromStatus())
                        .toStatus(history.getToStatus())
                        .changedByEmail(history.getChangedBy().getEmail())
                        .changedAt(history.getChangedAt())
                        .note(history.getNote())
                        .build())
                .toList();
    }

    /**
     * The only path that changes an application's status. It also records the
     * transition so the application and its audit history stay consistent.
     */
    private void transition(JobApplication application, StatusType newStatus, User actor, String note) {
        StatusType currentStatus = application.getStatus();
        boolean isInitialApplication = currentStatus == null && newStatus == StatusType.APPLIED;
        boolean isAllowed = isInitialApplication || VALID_TRANSITIONS
                .getOrDefault(currentStatus, Set.of())
                .contains(newStatus);

        if (!isAllowed) {
            throw new IllegalStateException(
                    "Invalid application status transition from " + currentStatus + " to " + newStatus
            );
        }

        application.setStatus(newStatus);
        jobApplicationRepository.save(application);

        applicationStatusHistoryRepository.save(ApplicationStatusHistory.builder()
                .jobApplication(application)
                .fromStatus(currentStatus)
                .toStatus(newStatus)
                .changedBy(actor)
                .changedAt(LocalDateTime.now())
                .note(note)
                .build());
    }

    private boolean canViewApplicationHistory(User user, JobApplication application) {
        if (user.getRole() == RoleType.CANDIDATE) {
            CandidateProfile profile = application.getCandidateProfile();
            return profile.getUser().getId().equals(user.getId());
        }

        if (user.getRole() == RoleType.RECRUITER) {
            RecruiterProfile recruiter = application.getJob().getCreatedBy();
            return recruiter.getUser().getId().equals(user.getId());
        }

        return false;
    }

    private boolean isTerminal(StatusType status) {
        return status == StatusType.HIRED
                || status == StatusType.REJECTED
                || status == StatusType.WITHDRAWN;
    }

    private JobApplicationResponseDTO toResponse(JobApplication jobApplication) {
        return new JobApplicationResponseDTO(
                jobApplication.getId(),
                jobApplication.getJob().getTitle(),
                jobApplication.getStatus(),
                jobApplication.getMatchScore()
        );
    }
}
