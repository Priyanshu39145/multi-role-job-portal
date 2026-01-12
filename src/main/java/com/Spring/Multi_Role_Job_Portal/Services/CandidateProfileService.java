package com.Spring.Multi_Role_Job_Portal.Services;

import com.Spring.Multi_Role_Job_Portal.DTO.CandidateProfileRequestDTO;
import com.Spring.Multi_Role_Job_Portal.DTO.CandidateProfileResponseDTO;
import com.Spring.Multi_Role_Job_Portal.Entities.CandidateProfile;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.RoleType;
import com.Spring.Multi_Role_Job_Portal.Entities.User;
import com.Spring.Multi_Role_Job_Portal.Repositories.CandidateProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final ModelMapper modelMapper;

    //Very imp insight --- we have the current user --- we can get it from the SecurityContextHolder ---
    public CandidateProfileResponseDTO createProfile(CandidateProfileRequestDTO candidateProfileRequestDTO) throws AccessDeniedException {

        //We have to verify the user who is approaching to create the profile --
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //If the user doesnt exist or doesnt have the role of a candidate then we give Exception
        //We dont need this cause we check the role in the Security Filter Chain only ---
//        if(user==null || user.getRole()!= RoleType.CANDIDATE)
//            throw new AccessDeniedException("User is not valid to create a profile here");

        //Since the userId is the Pk in candidate profile --- we try to fetch the profile using the user ---
        //If the candidate profile exists then we return exception
        CandidateProfile candidateProfile = candidateProfileRepository.findByUser(user).orElse(null);

        if(candidateProfile!=null)
            throw new DataIntegrityViolationException("Candidate Profile already exists");

        candidateProfile = CandidateProfile.builder()
                .fullName(candidateProfileRequestDTO.getFullName())
                .phonenum(candidateProfileRequestDTO.getPhonenum())
                .resumeUrl(candidateProfileRequestDTO.getResumeUrl())
                .skills(candidateProfileRequestDTO.getSkills())
                .experienceYears(candidateProfileRequestDTO.getExperienceYears())
                .user(user)
                .build();

//        System.out.println(candidateProfile.toString());

        candidateProfileRepository.save(candidateProfile);

        return modelMapper.map(candidateProfile, CandidateProfileResponseDTO.class);
    }

    public CandidateProfileResponseDTO getProfile() throws AccessDeniedException {


        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //We dont need this cause we check the role in the Security Filter Chain only ---
//        if(user==null || user.getRole()!= RoleType.CANDIDATE)
//            throw new AccessDeniedException("User is not valid to get a profile here");

        //We always find the candidate profile using the current users id --- in this way the user doesnt has to supply the id --- to get the profile ---
        CandidateProfile candidateProfile = candidateProfileRepository.findByUser(user).orElse(null);

        if(candidateProfile==null)
            throw new EntityNotFoundException("Candidate Profile not found");

        return modelMapper.map(candidateProfile , CandidateProfileResponseDTO.class);

    }

    public CandidateProfileResponseDTO updateProfile(Map<String , Object> map) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //We dont need this cause we check the role in the Security Filter Chain only ---
//        if(user==null || user.getRole()!= RoleType.CANDIDATE)
//            throw new AccessDeniedException("User is not valid to update a profile here");

        CandidateProfile candidateProfile = candidateProfileRepository.findByUser(user).orElse(null);

        if(candidateProfile==null)
            throw new EntityNotFoundException("Candidate Profile not found");

        //Here we are patching the user --
        //We are updating the user on basis of the fields we are getting for update ---
        map.forEach((field,value)-> {
            //We do a switch case on field ---- checking each cases where we need to make changes
            switch(field) {
                case "fullName":
                    candidateProfile.setFullName(value.toString()); //We change value to String as it is of Object type Map<String,Object>
                    break;
                case "phonenum":
                    candidateProfile.setPhonenum(value.toString());
                    break;
                case "resumeUrl":
                    candidateProfile.setResumeUrl(value.toString());
                    break;
                case "skills":
                    Set<String> skills = new HashSet<>();
                    //We convert the value to Iterable type so that we can iterate it --- to find the skills
                    for (Object obj : (Iterable<?>) value) {
                        skills.add(obj.toString());
                    }
                    candidateProfile.setSkills(skills);
                    break;
                case "experienceYears":
                    candidateProfile.setExperienceYears(Integer.parseInt(value.toString()));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid field: " + field);

            }});

        candidateProfileRepository.save(candidateProfile);

        return modelMapper.map(candidateProfile , CandidateProfileResponseDTO.class);

    }
}
