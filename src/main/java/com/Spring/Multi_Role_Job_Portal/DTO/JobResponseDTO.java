package com.Spring.Multi_Role_Job_Portal.DTO;

import com.Spring.Multi_Role_Job_Portal.Entities.Type.EmploymentType;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobResponseDTO {
    private String title;
    private String description;
    private Set<String> requiredSkills;
    private Long id;
    private String companyName;
    private String location;
    private Integer experienceRequired;
    private Double expectedSalary;
    private EmploymentType employmentType;
    private JobStatus status;
}
