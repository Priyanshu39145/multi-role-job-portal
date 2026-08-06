package com.Spring.Multi_Role_Job_Portal.DTO;

import com.Spring.Multi_Role_Job_Portal.Entities.Type.StatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusHistoryResponseDTO {
    private StatusType fromStatus;
    private StatusType toStatus;
    private String changedByEmail;
    private LocalDateTime changedAt;
    private String note;
}
