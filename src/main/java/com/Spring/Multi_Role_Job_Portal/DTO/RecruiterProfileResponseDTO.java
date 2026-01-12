package com.Spring.Multi_Role_Job_Portal.DTO;

import com.Spring.Multi_Role_Job_Portal.Entities.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecruiterProfileResponseDTO {
    private Long id;
    private String fullName;
    private String contactEmail;
    private String contactPhone;
    private String location;
    private String companyName;
}
