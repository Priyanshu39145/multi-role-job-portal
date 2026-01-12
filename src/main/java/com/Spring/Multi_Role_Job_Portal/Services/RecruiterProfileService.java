package com.Spring.Multi_Role_Job_Portal.Services;

import com.Spring.Multi_Role_Job_Portal.DTO.RecruiterProfileResponseDTO;
import com.Spring.Multi_Role_Job_Portal.DTO.RecruiterProfileRequestDTO;
import com.Spring.Multi_Role_Job_Portal.Entities.Company;
import com.Spring.Multi_Role_Job_Portal.Entities.RecruiterProfile;
import com.Spring.Multi_Role_Job_Portal.Entities.Type.RoleType;
import com.Spring.Multi_Role_Job_Portal.Entities.User;
import com.Spring.Multi_Role_Job_Portal.Repositories.CompanyRepository;
import com.Spring.Multi_Role_Job_Portal.Repositories.RecruiterProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecruiterProfileService {

    private final RecruiterProfileRepository recruiterProfileRepository;
    private final CompanyRepository companyRepository;

    public RecruiterProfileResponseDTO createProfile(RecruiterProfileRequestDTO recruiterProfileRequestDTO) throws AccessDeniedException {

        //We have to verify the user who is approaching to create the profile --
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //If the user doesnt exist or doesnt have the role of a candidate then we give Exception
        //We dont need this cause we check the role in the Security Filter Chain only ---
//        if(user==null || user.getRole()!= RoleType.RECRUITER)
//            throw new AccessDeniedException("User is not valid to create a profile here");

        //Since the userId is the Pk in recruiter profile --- we try to fetch the profile using the user ---
        //If the candidate profile exists then we return exception
        RecruiterProfile recruiterProfile = recruiterProfileRepository.findByUser(user).orElse(null);

        if(recruiterProfile!=null)
            throw new DataIntegrityViolationException("Recruiter Profile already exists");

        //Inside the RequestDTO --- we have given the companyName ---
        //We first fetch the Company using the name --- then check if it exists
        //If it doesnt exist then we create a new Company entry using the name ---
        Company company = companyRepository.findByName(recruiterProfileRequestDTO.getCompanyName()).orElse(null);

        if(company==null)   {
            //If company is not found we make one ---
            company = Company.builder()
                    .name(recruiterProfileRequestDTO.getCompanyName())
                    .build();
            companyRepository.save(company);
        }

        recruiterProfile = RecruiterProfile.builder()
                .user(user)
                .fullName(recruiterProfileRequestDTO.getFullName())
                .company(company)
                .contactEmail(recruiterProfileRequestDTO.getContactEmail())
                .contactPhone(recruiterProfileRequestDTO.getContactPhone())
                .location(recruiterProfileRequestDTO.getLocation())
                .build();

//        System.out.println(candidateProfile.toString());

        recruiterProfileRepository.save(recruiterProfile);

        //We are making the ResponseDTO --- manually --- as it takes the Company name ---- not the COmpany Object
        RecruiterProfileResponseDTO recruiterProfileResponseDTO = RecruiterProfileResponseDTO.builder()
                .id(recruiterProfile.getId())
                .fullName(recruiterProfile.getFullName())
                .contactEmail(recruiterProfile.getContactEmail())
                .contactPhone(recruiterProfile.getContactPhone())
                .location(recruiterProfile.getLocation())
                .companyName(company.getName())
                .build();
        return recruiterProfileResponseDTO;
    }


    public RecruiterProfileResponseDTO getProfile() throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //We dont need this cause we check the role in the Security Filter Chain only ---
//        if(user==null || user.getRole()!= RoleType.RECRUITER)
//            throw new AccessDeniedException("User is not valid to get a profile here");

        RecruiterProfile recruiterProfile = recruiterProfileRepository.findByUser(user).orElse(null);

        if(recruiterProfile==null)
            throw new EntityNotFoundException("Recruiter Profile cant be found");

        RecruiterProfileResponseDTO recruiterProfileResponseDTO = RecruiterProfileResponseDTO.builder()
                .id(recruiterProfile.getId())
                .fullName(recruiterProfile.getFullName())
                .contactEmail(recruiterProfile.getContactEmail())
                .contactPhone(recruiterProfile.getContactPhone())
                .location(recruiterProfile.getLocation())
                .companyName(recruiterProfile.getCompany().getName())
                .build();
        return recruiterProfileResponseDTO;
    }


    public RecruiterProfileResponseDTO updateProfile(Map<String, Object> map) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //We dont need this cause we check the role in the Security Filter Chain only ---
//        if(user==null || user.getRole()!= RoleType.RECRUITER)
//            throw new AccessDeniedException("User is not valid to update a profile here");

        RecruiterProfile recruiterProfile = recruiterProfileRepository.findByUser(user).orElse(null);

        if(recruiterProfile==null)
            throw new EntityNotFoundException("Recruiter Profile cant be found");

        map.forEach((field,value) -> {
            switch(field)   {
                case "fullName" :
                    recruiterProfile.setFullName(value.toString());
                    break;
                case "contactEmail" :
                    recruiterProfile.setContactEmail(value.toString());
                    break;
                case "contactPhone" :
                    recruiterProfile.setContactPhone(value.toString());
                    break;
                case "location" :
                    recruiterProfile.setLocation(value.toString());
                    break;
                case "companyName" :
                    Company company = companyRepository.findByName(value.toString()).orElse(null);
                    if(company==null)   {
                        company = Company.builder()
                                .name(value.toString())
                                .build();
                        companyRepository.save(company);
                    }
                    recruiterProfile.setCompany(company);
                    break;
                default:
                    throw new IllegalArgumentException("Wrong field added");
            }
        });
        recruiterProfileRepository.save(recruiterProfile);
        return getProfile();
    }
}
