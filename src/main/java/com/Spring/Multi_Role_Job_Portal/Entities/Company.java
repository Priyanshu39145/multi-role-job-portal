package com.Spring.Multi_Role_Job_Portal.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true , nullable = false)
    private String name;

    private String website;
    private String description;

    private String industry;

    //One company can have many Recruiters ----
    @OneToMany(mappedBy = "company" , cascade = CascadeType.ALL)
    private List<RecruiterProfile> recruiters;

    @OneToMany(mappedBy = "company" , cascade = CascadeType.ALL)
    private List<Job> jobs;
}
