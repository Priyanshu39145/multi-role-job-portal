package com.Spring.Multi_Role_Job_Portal.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateProfileRequestDTO {
    @NotBlank(message = "Name should not be blank")
    private String fullName;

    private String phonenum;
    @NotBlank(message = "Recruiters expect a resume")
    private String resumeUrl;

    private Set<String> skills;
    private Integer experienceYears;
}
