package com.Spring.Multi_Role_Job_Portal.Entities;

import com.Spring.Multi_Role_Job_Portal.Entities.Type.EmploymentType;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.JobStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String description;

    @ElementCollection
    private Set<String> requiredSkills;

    private Integer experienceRequired;
    private String location;
    private Double expectedSalary;

    //Many Jobs can be of a single company
    @ManyToOne(fetch = FetchType.EAGER)
    private Company company;

    //Many Jobs can be created By a same recruiter
    @ManyToOne(fetch = FetchType.EAGER)
    private RecruiterProfile createdBy;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.OPEN;

    @OneToMany(mappedBy = "job")
    private List<JobApplication> jobApplicationList;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
