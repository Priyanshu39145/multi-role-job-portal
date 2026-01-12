package com.Spring.Multi_Role_Job_Portal.Controllers;

import com.Spring.Multi_Role_Job_Portal.DTO.CandidateProfileRequestDTO;
import com.Spring.Multi_Role_Job_Portal.DTO.CandidateProfileResponseDTO;
import com.Spring.Multi_Role_Job_Portal.Entities.User;
import com.Spring.Multi_Role_Job_Portal.Services.CandidateProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/candidate")
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    @Operation(
            summary = "Create candidate profile",
            description = "Creates a candidate profile for the currently authenticated candidate user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created successfully",
                    content = @Content(schema = @Schema(implementation = CandidateProfileResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid profile data"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not a candidate")
    })
    @PostMapping("/profile")
    public ResponseEntity<CandidateProfileResponseDTO> createCandidateProfile(@RequestBody @Valid CandidateProfileRequestDTO candidateProfileRequestDTO) throws AccessDeniedException {

        return ResponseEntity.status(HttpStatus.CREATED).body(candidateProfileService.createProfile( candidateProfileRequestDTO));
    }

    @Operation(
            summary = "Get candidate profile",
            description = "Returns the profile of the currently authenticated candidate"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile fetched successfully",
                    content = @Content(schema = @Schema(implementation = CandidateProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not a candidate"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<CandidateProfileResponseDTO> getCandidateProfile() throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.OK).body(candidateProfileService.getProfile());
    }

    @Operation(
            summary = "Update candidate profile",
            description = "Updates one or more fields of the currently authenticated candidate's profile"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = CandidateProfileResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update data"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not a candidate"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @PutMapping("/profile")
    public ResponseEntity<CandidateProfileResponseDTO> updateCandidateProfile(@RequestBody Map<String , Object> map) throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(candidateProfileService.updateProfile(map));
    }
}
