package com.Spring.Multi_Role_Job_Portal.Repositories;

import com.Spring.Multi_Role_Job_Portal.Entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String companyName);
}