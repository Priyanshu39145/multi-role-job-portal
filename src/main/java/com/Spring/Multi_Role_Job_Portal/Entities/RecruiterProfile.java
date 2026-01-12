package com.Spring.Multi_Role_Job_Portal.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.cglib.core.Local;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecruiterProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(unique = true)
    private User user;

    private String fullName;

    //We have another Entity Company --- see the Entity ---
    //The mapping is Many to One as many recruiters can be of same company ----
    @ManyToOne(fetch = FetchType.EAGER)
    private Company company;

    @Column(nullable = false )
    private String contactEmail;

    @Column(unique = true)
    private String contactPhone;

    private String location;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "createdBy") //We write the variable name there in mappedBy
    private List<Job> jobsHandled;
}
