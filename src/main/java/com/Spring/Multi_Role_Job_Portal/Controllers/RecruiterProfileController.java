package com.Spring.Multi_Role_Job_Portal.Controllers;

import com.Spring.Multi_Role_Job_Portal.DTO.RecruiterProfileRequestDTO;
import com.Spring.Multi_Role_Job_Portal.DTO.RecruiterProfileResponseDTO;
import com.Spring.Multi_Role_Job_Portal.Services.RecruiterProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.Map;
//Similar to CandidateProfileController ---
@RestController
@RequiredArgsConstructor
@RequestMapping("/recruiter")
public class RecruiterProfileController {

    private final RecruiterProfileService recruiterProfileService;

    @Operation(
            summary = "Create recruiter profile",
            description = "Creates a recruiter profile for the currently authenticated recruiter user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created successfully",
                    content = @Content(schema = @Schema(implementation = RecruiterProfileResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid profile data"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not a recruiter")
    })
    @PostMapping("/profile")
    public ResponseEntity<RecruiterProfileResponseDTO> createRecruiterProfile(@RequestBody @Valid RecruiterProfileRequestDTO recruiterProfileRequestDTO) throws AccessDeniedException {

        return ResponseEntity.status(HttpStatus.CREATED).body(recruiterProfileService.createProfile(recruiterProfileRequestDTO));
    }

    @Operation(
            summary = "Get recruiter profile",
            description = "Returns the profile of the currently authenticated recruiter"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile fetched successfully",
                    content = @Content(schema = @Schema(implementation = RecruiterProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not a recruiter"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<RecruiterProfileResponseDTO> getRecruiterProfile() throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.OK).body(recruiterProfileService.getProfile());
    }

    @Operation(
            summary = "Update recruiter profile",
            description = "Updates one or more fields of the currently authenticated recruiter's profile"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = RecruiterProfileResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update data"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not a recruiter"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @PutMapping("/profile")
    public ResponseEntity<RecruiterProfileResponseDTO> updateRecruiterProfile(@RequestBody Map<String , Object> map) throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(recruiterProfileService.updateProfile(map));
    }
}
