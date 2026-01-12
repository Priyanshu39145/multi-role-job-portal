package com.Spring.Multi_Role_Job_Portal.Services;

import com.Spring.Multi_Role_Job_Portal.DTO.RegisterRequestDTO;
import com.Spring.Multi_Role_Job_Portal.DTO.RegisterResponseDTO;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.RoleType;
import com.Spring.Multi_Role_Job_Portal.Entities.User;
import com.Spring.Multi_Role_Job_Portal.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
//    private final HandlerExceptionResolver handlerExceptionResolver;

    private User createUser(RegisterRequestDTO registerRequestDTO)  {
        User user = userRepository.findByEmail(registerRequestDTO.getEmail()).orElse(null);

        if(user!=null)  {
            throw new DataIntegrityViolationException("User here exists");
        }

        user = User.builder()
                .email(registerRequestDTO.getEmail())
                .password(passwordEncoder.encode(registerRequestDTO.getPassword()))
                .enabled(true)
                .build();
        return user;
    }


    public RegisterResponseDTO registerRecruiter(RegisterRequestDTO registerRequestDTO) {
        User user = createUser(registerRequestDTO);

        user.setRole(RoleType.RECRUITER);

        userRepository.save(user);

        return modelMapper.map(user , RegisterResponseDTO.class);
    }


    public RegisterResponseDTO registerCandidate(RegisterRequestDTO registerRequestDTO) {
        User user = createUser(registerRequestDTO);

        user.setRole(RoleType.CANDIDATE);

        userRepository.save(user);

        return modelMapper.map(user , RegisterResponseDTO.class);
    }
}
