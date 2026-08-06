package com.Spring.Multi_Role_Job_Portal.Repositories;

import com.Spring.Multi_Role_Job_Portal.Entities.ApplicationStatusHistory;
import com.Spring.Multi_Role_Job_Portal.Entities.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {
    List<ApplicationStatusHistory> findAllByJobApplicationOrderByChangedAtAsc(JobApplication jobApplication);
}
