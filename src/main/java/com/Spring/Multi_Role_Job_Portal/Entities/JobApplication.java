package com.Spring.Multi_Role_Job_Portal.Entities;

import com.Spring.Multi_Role_Job_Portal.Entities.Type.StatusType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.engine.profile.Fetch;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_job_candidate" , columnNames = {"job" , "candidateProfile"})
        },
        indexes = {
                @Index(name = "idx_status" , columnList = "status")
        }
)
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Job job;

    @ManyToOne
    private CandidateProfile candidateProfile;

    @Enumerated(EnumType.STRING)
    private StatusType status;

    @CreationTimestamp
    private LocalDateTime appliedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
