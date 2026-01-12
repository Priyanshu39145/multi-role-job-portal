package com.Spring.Multi_Role_Job_Portal.DTO;

import com.Spring.Multi_Role_Job_Portal.Entities.Type.EmploymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobRequestDTO {
    @NotBlank(message = "Job Title must not be null")
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private Set<String> requiredSkills;
    @NotBlank
    private Integer experienceRequired;

    private String location;
    @NotBlank
    private Double expectedSalary;
    @NotBlank
    private EmploymentType employmentType;

}
