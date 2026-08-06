package com.Spring.Multi_Role_Job_Portal.Repositories;

import com.Spring.Multi_Role_Job_Portal.Entities.CandidateProfile;
import com.Spring.Multi_Role_Job_Portal.Entities.Job;
import com.Spring.Multi_Role_Job_Portal.Entities.JobApplication;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByJobAndCandidateProfileAndStatusIn(
            Job job,
            CandidateProfile candidateProfile,
            Set<StatusType> statuses
    );

    boolean existsByJobAndCandidateProfileAndStatusAndUpdatedAtAfter(
            Job job,
            CandidateProfile candidateProfile,
            StatusType status,
            LocalDateTime updatedAt
    );

    List<JobApplication> findAllByCandidateProfile(CandidateProfile candidateProfile);


    List<JobApplication> findAllByJob(Job job);

    List<JobApplication> findAllByJobOrderByMatchScoreDesc(Job job);

    List<JobApplication> findAllByJobAndStatus(Job job, StatusType statusType);

    List<JobApplication> findByJob(Job job);
}
