package com.Spring.Multi_Role_Job_Portal.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecruiterProfileRequestDTO {

    @NotBlank(message = "Name cant be blank")
    private String fullName;
    @NotBlank(message = "Recruiter must be of a company")
    private String companyName;
    @NotBlank(message = "Email should not be blank")
    @Email
    private String contactEmail;
    private String contactPhone;
    private String location;
}
