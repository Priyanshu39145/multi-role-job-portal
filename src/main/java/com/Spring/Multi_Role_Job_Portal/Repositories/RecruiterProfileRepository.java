package com.Spring.Multi_Role_Job_Portal.Repositories;

import com.Spring.Multi_Role_Job_Portal.Entities.RecruiterProfile;
import com.Spring.Multi_Role_Job_Portal.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RecruiterProfileRepository extends JpaRepository<RecruiterProfile, Long> {
    Optional<RecruiterProfile> findByUser(User user);

    Optional<RecruiterProfile> findByFullName(String recruiterName);
}