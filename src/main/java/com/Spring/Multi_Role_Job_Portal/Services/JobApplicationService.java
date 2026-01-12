package com.Spring.Multi_Role_Job_Portal.Services;

import com.Spring.Multi_Role_Job_Portal.DTO.JobApplicationResponseDTO;
import com.Spring.Multi_Role_Job_Portal.Entities.*;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.JobStatus;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.RoleType;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.StatusType;
import com.Spring.Multi_Role_Job_Portal.Repositories.CandidateProfileRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.JobApplicationRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.JobRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.RecruiterProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

//import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobRepository jobRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final ModelMapper modelMapper;

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

        JobApplication jobApplication = JobApplication.builder()
                .job(job)
                .candidateProfile(candidateProfile)
                .status(StatusType.APPLIED)
                .build();

        boolean alreadyApplied =
                jobApplicationRepository.existsByJobAndCandidateProfile(job, candidateProfile);

        if (alreadyApplied)
            throw new DataIntegrityViolationException("You have already applied to this job");

        jobApplicationRepository.save(jobApplication);

        return new JobApplicationResponseDTO(jobApplication.getId() , jobApplication.getJob().getTitle() , jobApplication.getStatus());

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
                .map(jobApplication -> new JobApplicationResponseDTO(jobApplication.getId() , jobApplication.getJob().getTitle() , jobApplication.getStatus()))
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

        List<JobApplication> jobApplications = jobApplicationRepository.findAllByJob(job);

        return jobApplications.stream()
                .map(jobApplication -> new JobApplicationResponseDTO(jobApplication.getId() , jobApplication.getJob().getTitle() , jobApplication.getStatus()))
                .toList();
    }


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

        if (jobApplication.getStatus() != StatusType.APPLIED)
            throw new DataIntegrityViolationException("Application already processed");

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalStateException("Cannot shortlist for closed job");
        }

        Set<String> requiredSkills = job.getRequiredSkills();
        Set<String> skills = candidateProfile.getSkills();

        if(requiredSkills.isEmpty() || skills.isEmpty())
            throw new EntityNotFoundException("Skills not available");

        boolean flag = true;

        for(String s : requiredSkills)  {
            if(!skills.contains(s)) {
                flag = false;
                break;
            }
        }
        if(job.getExperienceRequired()>jobApplication.getCandidateProfile().getExperienceYears())
            flag = false;

        if(flag)    {
            jobApplication.setStatus(StatusType.SHORTLISTED);
        }
        else
            jobApplication.setStatus(StatusType.REJECTED);

        jobApplicationRepository.save(jobApplication);

        return new JobApplicationResponseDTO(jobApplication.getId() , jobApplication.getJob().getTitle() , jobApplication.getStatus());

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
                .map(jobApplication -> new JobApplicationResponseDTO(jobApplication.getId() , jobApplication.getJob().getTitle() , jobApplication.getStatus()))
                .toList();

    }


    @Transactional
    public JobApplicationResponseDTO hireCandidate(Long applicationId) throws AccessDeniedException {
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
        selected.setStatus(StatusType.HIRED);

        // 2️⃣ Reject all others
        List<JobApplication> all = jobApplicationRepository.findByJob(job);
        for (JobApplication app : all) {
            if (!app.getId().equals(applicationId)) {
                app.setStatus(StatusType.REJECTED);
            }
        }

        // 3️⃣ Close the job
        job.setStatus(JobStatus.CLOSED);

        jobRepository.save(job);
        jobApplicationRepository.saveAll(all);

        return new JobApplicationResponseDTO(selected.getId() , selected.getJob().getTitle() , selected.getStatus());
    }
}
