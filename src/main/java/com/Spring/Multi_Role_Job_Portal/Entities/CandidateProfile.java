package com.Spring.Multi_Role_Job_Portal.Entities;

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
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String phonenum;

    private String resumeUrl;

    @ElementCollection
    private Set<String> skills;

    private Integer experienceYears;

    //MapsId --- will make the userId the primary Key ---
    @OneToOne
//    @MapsId
    @JoinColumn(unique = true)
    private User user;

    @OneToMany(mappedBy = "candidateProfile")
    private List<JobApplication> jobApplicationList;

    //Updates during Creation
    @CreationTimestamp
    private LocalDateTime createdAt;
    //Updates during Updation ---
    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
