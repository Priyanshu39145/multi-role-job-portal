package com.Spring.Multi_Role_Job_Portal.Services;

import com.Spring.Multi_Role_Job_Portal.Entities.CandidateProfile;
import com.Spring.Multi_Role_Job_Portal.Entities.Job;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calculates a candidate's compatibility with a job. This service intentionally
 * contains no persistence or application-status concerns.
 */
@Service
public class MatchingService {

    private static final double SKILLS_WEIGHT = 60.0;
    private static final double EXPERIENCE_WEIGHT = 25.0;
    private static final double LOCATION_WEIGHT = 10.0;
    private static final double SALARY_WEIGHT = 5.0;

    public double computeMatchScore(Job job, CandidateProfile candidate) {
        double score = skillScore(job.getRequiredSkills(), candidate.getSkills())
                + experienceScore(job.getExperienceRequired(), candidate.getExperienceYears())
                + locationScore(job.getLocation(), candidate.getLocation())
                + salaryScore(job.getExpectedSalary(), candidate.getExpectedSalary());

        return Math.max(0.0, Math.min(100.0, score));
    }

    private double skillScore(Set<String> requiredSkills, Set<String> candidateSkills) {
        Set<String> normalizedRequiredSkills = normalizeSkills(requiredSkills);
        if (normalizedRequiredSkills.isEmpty()) {
            return 0.0;
        }

        Set<String> normalizedCandidateSkills = normalizeSkills(candidateSkills);
        long matchingSkills = normalizedRequiredSkills.stream()
                .filter(normalizedCandidateSkills::contains)
                .count();

        return ((double) matchingSkills / normalizedRequiredSkills.size()) * SKILLS_WEIGHT;
    }

    private double experienceScore(Integer requiredExperience, Integer candidateExperience) {
        int required = Math.max(0, requiredExperience == null ? 0 : requiredExperience);
        int candidate = Math.max(0, candidateExperience == null ? 0 : candidateExperience);

        if (required == 0 || candidate >= required) {
            return EXPERIENCE_WEIGHT;
        }

        return ((double) candidate / required) * EXPERIENCE_WEIGHT;
    }

    private double locationScore(String jobLocation, String candidateLocation) {
        if (isBlank(jobLocation) || isBlank(candidateLocation)) {
            return 0.0;
        }

        return jobLocation.trim().equalsIgnoreCase(candidateLocation.trim()) ? LOCATION_WEIGHT : 0.0;
    }

    private double salaryScore(Double jobExpectedSalary, Double candidateExpectedSalary) {
        if (jobExpectedSalary == null || candidateExpectedSalary == null) {
            return 0.0;
        }

        return candidateExpectedSalary <= jobExpectedSalary ? SALARY_WEIGHT : 0.0;
    }

    private Set<String> normalizeSkills(Set<String> skills) {
        if (skills == null) {
            return Collections.emptySet();
        }

        return skills.stream()
                .filter(skill -> !isBlank(skill))
                .map(skill -> skill.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
