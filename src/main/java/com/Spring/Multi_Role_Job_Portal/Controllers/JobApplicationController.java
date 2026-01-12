package com.Spring.Multi_Role_Job_Portal.Controllers;

import com.Spring.Multi_Role_Job_Portal.DTO.JobApplicationResponseDTO;
import com.Spring.Multi_Role_Job_Portal.DTO.JobResponseDTO;
import com.Spring.Multi_Role_Job_Portal.Services.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @Operation(
            summary = "Apply to a job",
            description = "Allows a candidate to apply for a job using the job ID. A candidate can apply only once per job."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Application submitted successfully",
                    content = @Content(schema = @Schema(implementation = JobApplicationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Candidate already applied to this job"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only candidates can apply to jobs"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PostMapping("/jobs/{jobId}/apply")
    public ResponseEntity<JobApplicationResponseDTO> applyToJobById(@PathVariable Long jobId) throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobApplicationService.applyToJobById(jobId));
    }

    @Operation(
            summary = "Get candidate applications",
            description = "Returns all job applications submitted by the currently logged-in candidate"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Applications fetched successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only candidates can access this")
    })
    @GetMapping("/candidate/applications")
    public ResponseEntity<List<JobApplicationResponseDTO>> getAllApplications() throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.OK).body((jobApplicationService.getAllApplications()));
    }

    @Operation(
            summary = "Get applications for a job",
            description = "Allows a recruiter to view all applications for a job they posted"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Applications fetched successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only the recruiter who owns the job can view applications"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/recruiter/jobs/{jobId}/applications")
    public ResponseEntity<List<JobApplicationResponseDTO>> getAllApplicationsForRecruiter(@PathVariable Long jobId) throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.OK).body(jobApplicationService.getAllApplicationsForRecruiter(jobId));
    }

    @Operation(
            summary = "Shortlist an application",
            description = "Allows a recruiter to shortlist a candidate’s job application"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application shortlisted successfully",
                    content = @Content(schema = @Schema(implementation = JobApplicationResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only the recruiter who owns the job can shortlist"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PatchMapping("/applications/{applicationId}")
    public ResponseEntity<JobApplicationResponseDTO> shortList(@PathVariable Long applicationId) throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.OK).body(jobApplicationService.shortList(applicationId));
    }

    @Operation(
            summary = "Get shortlisted applications",
            description = "Returns all shortlisted candidates for a specific job"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shortlisted candidates fetched successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only the recruiter who owns the job can view shortlisted candidates"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/applications/shortListed/{jobId}")
    public ResponseEntity<List<JobApplicationResponseDTO>> getAllShortListedApplications(@PathVariable Long jobId) throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.OK).body((jobApplicationService.getAllShortListedApplications(jobId)));
    }



    @Operation(
            summary = "Hire a Candidate for a Job",
            description = "Allows a recruiter to hire a shortlisted candidate. "
                    + "Once hired, the job will be automatically closed and all other applications "
                    + "for that job will be rejected."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Candidate successfully hired",
                    content = @Content(schema = @Schema(implementation = JobApplicationResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – user is not authenticated"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – user is not the recruiter who owns this job"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Job application not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Job is already closed or a candidate has already been hired"
            )
    })
    @PatchMapping("/recruiter/applications/{applicationId}/hire")
    public ResponseEntity<JobApplicationResponseDTO> hireCandidate(@PathVariable Long applicationId)
            throws AccessDeniedException {
        return ResponseEntity.ok(jobApplicationService.hireCandidate(applicationId));
    }

}
