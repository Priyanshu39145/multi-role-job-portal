package com.Spring.Multi_Role_Job_Portal.Controllers;


import com.Spring.Multi_Role_Job_Portal.DTO.RegisterRequestDTO;
import com.Spring.Multi_Role_Job_Portal.DTO.RegisterResponseDTO;
import com.Spring.Multi_Role_Job_Portal.Services.RegisterService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegistrationController {

    private final RegisterService registerService;


    //Here we are documenting our controller method ---- endpoint method ---
    @Operation(
            summary = "Register a Recruiter", //Giving a description and summary which will be displayed on the UI
            description = "Creates a new recruiter account using email and password"
    )
    @ApiResponses({
            //If the response is correct --- we give 201 response code in the UI --- and then show the Content where we define the schema which will be shown ----
//            This tells Swagger what responses this API can return
            @ApiResponse(responseCode = "201", description = "Recruiter registered successfully",
                    content = @Content(schema = @Schema(implementation = RegisterResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/recruiter")
    public ResponseEntity<RegisterResponseDTO> registerRecruiter(@RequestBody @Valid RegisterRequestDTO registerRequestDTO)    {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.registerRecruiter(registerRequestDTO));
    }

    @Operation(
            summary = "Register a Candidate",
            description = "Creates a new candidate account using email and password"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Candidate registered successfully",
                    content = @Content(schema = @Schema(implementation = RegisterResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/candidate")
    public ResponseEntity<RegisterResponseDTO> registerCandidate(@RequestBody @Valid RegisterRequestDTO registerRequestDTO)    {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.registerCandidate(registerRequestDTO));
    }

}
