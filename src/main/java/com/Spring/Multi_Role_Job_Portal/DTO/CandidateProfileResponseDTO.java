package com.Spring.Multi_Role_Job_Portal.DTO;

import jakarta.persistence.ElementCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateProfileResponseDTO {
    private Long id;
    private String fullName;

    private String phonenum;

    private String resumeUrl;


    private Set<String> skills;

    private Integer experienceYears;
}
