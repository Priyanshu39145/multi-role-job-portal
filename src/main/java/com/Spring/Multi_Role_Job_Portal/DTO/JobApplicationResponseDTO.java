package com.Spring.Multi_Role_Job_Portal.DTO;

import com.Spring.Multi_Role_Job_Portal.Entities.Type.StatusType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobApplicationResponseDTO {
    private Long id;
    private String jobTitle;
    private StatusType status;
}
