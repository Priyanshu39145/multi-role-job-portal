package com.Spring.Multi_Role_Job_Portal.Repositories;

import com.Spring.Multi_Role_Job_Portal.Entities.Job;
import com.Spring.Multi_Role_Job_Portal.Entities.RecruiterProfile;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.EmploymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
////    Page<Job> findAll(Pageable page); It is a native method --- no pagination required separately ---
//   Page<Job> findByLocation(String location, Pageable pageable);
//    Page<Job> findByEmploymentType(EmploymentType employmentType, Pageable pageable);
//    Page<Job> findByCompany_Name(String companyName, Pageable pageable);
//    Page<Job> findByCreatedBy(RecruiterProfile recruiterProfile, Pageable pageable);
//    Page<Job> findByExperienceRequiredLessThanEqual(Integer experience, Pageable pageable);
//    Page<Job> findByLocationAndEmploymentType(
//            String location,
//            EmploymentType employmentType,
//            Pageable pageable
//    );
}
