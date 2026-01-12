package com.Spring.Multi_Role_Job_Portal.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    @NotBlank(message = "Email cant be blank")
    @Email(message = "Email should be valid")
    private String email;
    @NotBlank(message = "Password cant be blank")
    @Size(min = 6 , message = "Password must be atleast 6 characters long")
    private String password;
}
