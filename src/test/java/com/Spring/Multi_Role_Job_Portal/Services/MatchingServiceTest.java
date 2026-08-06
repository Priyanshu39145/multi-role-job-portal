package com.Spring.Multi_Role_Job_Portal.Services;

import com.Spring.Multi_Role_Job_Portal.Entities.CandidateProfile;
import com.Spring.Multi_Role_Job_Portal.Entities.Job;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchingServiceTest {

    private final MatchingService matchingService = new MatchingService();

    @Test
    void returnsOneHundredForACompleteMatch() {
        Job job = Job.builder()
                .requiredSkills(Set.of("Java", "Spring"))
                .experienceRequired(4)
                .location("Bengaluru")
                .expectedSalary(1500000.0)
                .build();
        CandidateProfile candidate = CandidateProfile.builder()
                .skills(Set.of("java", "SPRING", "SQL"))
                .experienceYears(5)
                .location(" bengaluru ")
                .expectedSalary(1400000.0)
                .build();

        assertEquals(100.0, matchingService.computeMatchScore(job, candidate));
    }

    @Test
    void calculatesPartialSkillsAndProportionalExperience() {
        Job job = Job.builder()
                .requiredSkills(Set.of("Java", "Spring", "React"))
                .experienceRequired(4)
                .location("Bengaluru")
                .expectedSalary(1500000.0)
                .build();
        CandidateProfile candidate = CandidateProfile.builder()
                .skills(Set.of("Java", "React"))
                .experienceYears(2)
                .location("Mumbai")
                .expectedSalary(1600000.0)
                .build();

        assertEquals(52.5, matchingService.computeMatchScore(job, candidate));
    }

    @Test
    void handlesMissingOptionalProfileDataWithoutRejectingTheCalculation() {
        Job job = Job.builder()
                .requiredSkills(Set.of("Java"))
                .experienceRequired(0)
                .location("Bengaluru")
                .expectedSalary(1500000.0)
                .build();
        CandidateProfile candidate = CandidateProfile.builder()
                .skills(null)
                .experienceYears(null)
                .build();

        assertEquals(25.0, matchingService.computeMatchScore(job, candidate));
    }
}
