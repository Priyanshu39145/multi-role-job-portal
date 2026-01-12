package com.Spring.Multi_Role_Job_Portal.Specifications;

import com.Spring.Multi_Role_Job_Portal.Entities.Job;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.EmploymentType;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.JobStatus;
import org.springframework.data.jpa.domain.Specification;

public class JobSpecification {

    //Here we are just defining filters or specifications
    //Specification<Job> means that this filter applies to the Job entity only
    public static Specification<Job> hasLocation(String location) {
        //root--- Job Table
        //query --- query that is being created ---
        //cb -- Criteria Builder --- Used to building conditions ---
        //We return null if we couldnt fetch the location
        // cb.equal(root.get("location"), location) --- means we are adding WHERE location = ? in the query
        return (root, query, cb) ->
                location == null ? null : cb.equal(root.get("location"), location);
    }

    public static Specification<Job> minExperience(Integer experience) {
        //cb.greaterThanOrEqualTo(root.get("experienceRequired"), experience) --- means we are adding WHERE experience>=experienceRequired
        return (root, query, cb) ->
                experience == null ? null :
                        cb.greaterThanOrEqualTo(root.get("experienceRequired"), experience);
    }

    public static Specification<Job> hasEmploymentType(EmploymentType type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("employmentType"), type);
    }

    public static Specification<Job> hasCompany(String companyName) {
        return (root, query, cb) ->
                companyName == null ? null :
                        cb.equal(root.get("company").get("name"), companyName);
    }

    public static Specification<Job> minExpectedSalary(Double salary) {
        return (root, query, cb) ->
                salary == null ? null :
                        cb.greaterThanOrEqualTo(root.get("expectedSalary"), salary);
    }

    public static Specification<Job> hasRequiredSkill(String skill) {
        //cb.isMember(skill, root.get("requiredSkills")) --- we check if the skill entered is part of the set inside the requiredSkills set ---
        return (root, query, cb) ->
                skill == null ? null :
                        cb.isMember(skill, root.get("requiredSkills"));
    }

    public static Specification<Job> isOpen() {
        return (root, query, cb) ->
                cb.equal(root.get("status"), JobStatus.OPEN);
    }
}