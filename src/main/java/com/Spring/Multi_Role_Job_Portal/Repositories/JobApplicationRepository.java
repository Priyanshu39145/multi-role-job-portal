package com.Spring.Multi_Role_Job_Portal.Repositories;

import com.Spring.Multi_Role_Job_Portal.Entities.CandidateProfile;
import com.Spring.Multi_Role_Job_Portal.Entities.Job;
import com.Spring.Multi_Role_Job_Portal.Entities.JobApplication;
import com.Spring.Multi_Role_Job_Portal.Entities.RecruiterProfile;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByJobAndCandidateProfile(Job job, CandidateProfile candidateProfile);

    List<JobApplication> findAllByCandidateProfile(CandidateProfile candidateProfile);


    List<JobApplication> findAllByJob(Job job);

    List<JobApplication> findAllByJobAndStatus(Job job, StatusType statusType);

    List<JobApplication> findByJob(Job job);
}