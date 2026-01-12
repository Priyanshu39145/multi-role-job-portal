package com.Spring.Multi_Role_Job_Portal.Services;

import com.Spring.Multi_Role_Job_Portal.DTO.JobApplicationResponseDTO;
import com.Spring.Multi_Role_Job_Portal.DTO.JobRequestDTO;
import com.Spring.Multi_Role_Job_Portal.DTO.JobResponseDTO;
import com.Spring.Multi_Role_Job_Portal.Entities.Company;
import com.Spring.Multi_Role_Job_Portal.Entities.Job;
import com.Spring.Multi_Role_Job_Portal.Entities.RecruiterProfile;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.EmploymentType;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.JobStatus;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.RoleType;
import com.Spring.Multi_Role_Job_Portal.Entities.User;
import com.Spring.Multi_Role_Job_Portal.Repositories.CompanyRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.JobRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.RecruiterProfileRepository;
import com.Spring.Multi_Role_Job_Portal.Specifications.JobSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final CompanyRepository companyRepository;
    private final ModelMapper modelMapper;

    public JobResponseDTO createJob(JobRequestDTO jobRequestDTO) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //Job is only crreated by recruiter
        if(user==null || user.getRole()!= RoleType.RECRUITER)
            throw new AccessDeniedException("User is not valid to create a job");

        RecruiterProfile recruiterProfile = recruiterProfileRepository.findByUser(user).orElse(null);

        if(recruiterProfile==null)
            throw new EntityNotFoundException("Recruiter is not found");

        Job job = Job.builder()
                .title(jobRequestDTO.getTitle())
                .description(jobRequestDTO.getDescription())
                .requiredSkills(jobRequestDTO.getRequiredSkills())
                .experienceRequired(jobRequestDTO.getExperienceRequired())
                .location(jobRequestDTO.getLocation())
                .expectedSalary(jobRequestDTO.getExpectedSalary())
                .company(recruiterProfile.getCompany())
                .createdBy(recruiterProfile)
                .employmentType(jobRequestDTO.getEmploymentType())
                .build();

        jobRepository.save(job);

        JobResponseDTO jobResponseDTO = JobResponseDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requiredSkills(job.getRequiredSkills())
                .companyName(job.getCompany().getName())
                .location(job.getLocation())
                .experienceRequired(job.getExperienceRequired())
                .expectedSalary(job.getExpectedSalary())
                .employmentType(job.getEmploymentType())
                .status(job.getStatus())
                .build();

        return jobResponseDTO;
    }


    public List<JobResponseDTO> getAllJobs(int page,int size) {
        Page<Job> jobs= jobRepository.findAll(PageRequest.of(page,size));

        return jobs.stream()
                .map(job -> JobResponseDTO.builder()
                        .id(job.getId())
                        .title(job.getTitle())
                        .description(job.getDescription())
                        .requiredSkills(job.getRequiredSkills())
                        .companyName(job.getCompany().getName())
                        .location(job.getLocation())
                        .experienceRequired(job.getExperienceRequired())
                        .expectedSalary(job.getExpectedSalary())
                        .employmentType(job.getEmploymentType())
                        .status(job.getStatus())
                        .build())
                .toList();
    }

    public JobResponseDTO getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId).orElse(null);

        if(job==null)
            throw new EntityNotFoundException("Job doesnt exist");

        JobResponseDTO jobResponseDTO = JobResponseDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requiredSkills(job.getRequiredSkills())
                .companyName(job.getCompany().getName())
                .location(job.getLocation())
                .experienceRequired(job.getExperienceRequired())
                .expectedSalary(job.getExpectedSalary())
                .employmentType(job.getEmploymentType())
                .status(job.getStatus())
                .build();

        return jobResponseDTO;

    }


    public JobResponseDTO updateJobById(Map<String, Object> map, Long jobId) throws AccessDeniedException {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user == null || user.getRole() != RoleType.RECRUITER)
            throw new AccessDeniedException("User is not valid to update a job");

        // Fetch recruiter profile
        RecruiterProfile recruiterProfile = recruiterProfileRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Recruiter profile not found"));

        // Fetch job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));

        // Ownership check
        if (!job.getCreatedBy().getId().equals(recruiterProfile.getId()))
            throw new AccessDeniedException("You are not allowed to update this job");

        // Patch fields
        map.forEach((field, value) -> {
            switch (field) {
                case "title":
                    job.setTitle(value.toString());
                    break;

                case "description":
                    job.setDescription(value.toString());
                    break;

                case "requiredSkills":
                    Set<String> skills = new HashSet<>();
                    for (Object obj : (Iterable<?>) value) {
                        skills.add(obj.toString());
                    }
                    job.setRequiredSkills(skills);
                    break;

                case "experienceRequired":
                    job.setExperienceRequired(Integer.parseInt(value.toString()));
                    break;

                case "location":
                    job.setLocation(value.toString());
                    break;

                case "expectedSalary":
                    job.setExpectedSalary(Double.parseDouble(value.toString()));
                    break;

                default:
                    throw new IllegalArgumentException("Invalid field for job update");
            }
        });

        jobRepository.save(job);

        JobResponseDTO jobResponseDTO = JobResponseDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requiredSkills(job.getRequiredSkills())
                .companyName(job.getCompany().getName())
                .location(job.getLocation())
                .experienceRequired(job.getExperienceRequired())
                .expectedSalary(job.getExpectedSalary())
                .employmentType(job.getEmploymentType())
                .status(job.getStatus())
                .build();

        return jobResponseDTO;

    }

    public Void deleteJobById(Long jobId) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user == null || user.getRole() != RoleType.RECRUITER)
            throw new AccessDeniedException("User is not valid to delete a job");

        Job job = jobRepository.findById(jobId).orElse(null);

        if(job==null)   {
            throw new EntityNotFoundException("Job is not there");
        }

        RecruiterProfile recruiterProfile = recruiterProfileRepository.findByUser(user).orElse(null);

        if(recruiterProfile==null)
            throw new EntityNotFoundException("Recruiter not available");

        if (!job.getCreatedBy().getId().equals(recruiterProfile.getId()))
            throw new AccessDeniedException("You cannot delete this job");


        jobRepository.deleteById(jobId);

        return null;

    }


    //Imp ---- Filtering jobs ----
    public Page<JobResponseDTO> searchJobs(
            String location,
            Integer minExperience,
            EmploymentType employmentType,
            String companyName,
            Double minSalary,
            String skill,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        //We make a Specification object containing all the filters --- which are the methods inside the JobSpecification class
        Specification<Job> spec = Specification
                .allOf(JobSpecification.hasLocation(location))
                .and(JobSpecification.minExperience(minExperience))
                .and(JobSpecification.hasEmploymentType(employmentType))
                .and(JobSpecification.hasCompany(companyName))
                .and(JobSpecification.minExpectedSalary(minSalary))
                .and(JobSpecification.hasRequiredSkill(skill));

        //We here execute the final query with the specifications ---

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Job> jobs = jobRepository.findAll(
                spec,
                PageRequest.of(page, size, sort)
        );

        return jobs
                .map(job -> JobResponseDTO.builder()
                        .id(job.getId())
                        .title(job.getTitle())
                        .description(job.getDescription())
                        .requiredSkills(job.getRequiredSkills())
                        .companyName(job.getCompany().getName())
                        .location(job.getLocation())
                        .experienceRequired(job.getExperienceRequired())
                        .expectedSalary(job.getExpectedSalary())
                        .employmentType(job.getEmploymentType())
                        .status(job.getStatus())
                        .build());

    }

    public JobResponseDTO closeJob(Long jobId) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user.getRole() != RoleType.RECRUITER) {
            throw new AccessDeniedException("Only recruiters can close jobs");
        }

        RecruiterProfile recruiter = recruiterProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Recruiter profile not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (!job.getCreatedBy().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You do not own this job");
        }

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalStateException("Job is already closed");
        }

        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);

        JobResponseDTO jobResponseDTO = JobResponseDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requiredSkills(job.getRequiredSkills())
                .companyName(job.getCompany().getName())
                .location(job.getLocation())
                .experienceRequired(job.getExperienceRequired())
                .expectedSalary(job.getExpectedSalary())
                .employmentType(job.getEmploymentType())
                .status(job.getStatus())
                .build();

        return jobResponseDTO;
    }
}
