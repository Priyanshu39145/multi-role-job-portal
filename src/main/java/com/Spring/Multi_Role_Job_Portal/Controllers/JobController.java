package com.Spring.Multi_Role_Job_Portal.Controllers;

import com.Spring.Multi_Role_Job_Portal.DTO.JobApplicationResponseDTO;
import com.Spring.Multi_Role_Job_Portal.DTO.JobRequestDTO;
import com.Spring.Multi_Role_Job_Portal.DTO.JobResponseDTO;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.EmploymentType;
import com.Spring.Multi_Role_Job_Portal.Services.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @Operation(
            summary = "Create a job posting",
            description = "Creates a new job posting. Only authenticated recruiters can create jobs."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Job created successfully",
                    content = @Content(schema = @Schema(implementation = JobResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid job data"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only recruiters can create jobs")
    })
    @PostMapping("/jobs")
    public ResponseEntity<JobResponseDTO> createJob(@RequestBody @Valid JobRequestDTO jobRequestDTO) throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(jobRequestDTO));
    }

    @Operation(
            summary = "Get all jobs (paginated)",
            description = "Returns a paginated list of all job postings. This endpoint is public."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully")
    })
    @GetMapping("/jobs")
    public ResponseEntity<List<JobResponseDTO>> getAllJobs(@RequestParam int page , @RequestParam int size) {
        return ResponseEntity.status(HttpStatus.OK).body(jobService.getAllJobs(page,size));
    }

    @Operation(
            summary = "Get job by ID",
            description = "Returns details of a specific job using its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job found",
                    content = @Content(schema = @Schema(implementation = JobResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobResponseDTO> getJobById(@PathVariable Long jobId)  {
        return ResponseEntity.status(HttpStatus.OK).body(jobService.getJobById(jobId));
    }

    @Operation(
            summary = "Update a job",
            description = "Updates a job posting. Only the recruiter who created the job can update it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job updated successfully",
                    content = @Content(schema = @Schema(implementation = JobResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update data"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only the job owner can update"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PutMapping("/jobs/{jobId}")
    public ResponseEntity<JobResponseDTO> updateJobById(@RequestBody Map<String , Object> map , @PathVariable Long jobId) throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.OK).body(jobService.updateJobById(map,jobId));
    }

    @Operation(
            summary = "Delete a job",
            description = "Deletes a job posting. Only the recruiter who created the job can delete it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Job deleted successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only the job owner can delete"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<Void> deleteJobById(@PathVariable Long jobId) throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(jobService.deleteJobById(jobId));
    }

    @Operation(
            summary = "Search jobs",
            description = "Search and filter job postings using multiple parameters with pagination and sorting"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully")
    })
    @GetMapping("/jobs/search")
    public ResponseEntity<Page<JobResponseDTO>> searchJobs(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(
                jobService.searchJobs(
                        location,
                        minExperience,
                        employmentType,
                        company,
                        minSalary,
                        skill,
                        page,
                        size,sortBy,direction
                )
        );
    }

    @PatchMapping("/jobs/{jobId}/close")
    public ResponseEntity<JobResponseDTO> closeJob(@PathVariable Long jobId) throws AccessDeniedException {
        return ResponseEntity.ok(jobService.closeJob(jobId));
    }





}
