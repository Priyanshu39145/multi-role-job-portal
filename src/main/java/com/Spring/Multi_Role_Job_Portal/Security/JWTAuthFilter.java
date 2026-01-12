package com.Spring.Multi_Role_Job_Portal.Security;
import com.Spring.Multi_Role_Job_Portal.Entities.User;
import com.Spring.Multi_Role_Job_Portal.Repositories.UserRepository;
import com.Spring.Multi_Role_Job_Portal.Security.AuthUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@RequiredArgsConstructor
//It is declared as a Component for generating a bean ---
@Component
//it is normal log framework from which we can display custom log messages inside the terminal --- log.info
@Slf4j
//Using JwtAuthFilter it has to extend the OncePerRequestFilter class and override the doFilterinternal method ----
//This method is responsible for the custom filter we are adding ---
public class JWTAuthFilter extends OncePerRequestFilter {

    private final AuthUtil authUtil;
    private final UserRepository userRepository;

    private final HandlerExceptionResolver handlerExceptionResolver;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        try {
            //We display a custom log message where we display the request URL ---
            log.info("incoming request: {}" , request.getRequestURI());

            //We first get the Request header where the JWT Token will be stored ---
            //We are getting the String under the header Authorization ---
            //In normal header JWT is stored as "Bearer ygruygrhjlqdio2rjkl.qVRUQVUOUQKEFBUWF.qkhgefvyvfutyvuovjqf" ---- Bearer space then the token
            final String header = request.getHeader("Authorization");

            //We check if the header is not null or valid
            if(header== null || !header.startsWith("Bearer "))   {
                //If the header is null or not valid(Doesnt start with Bearer) --- then we go on to the next Filter --- without setting the Security Context ----
                filterChain.doFilter(request,response);
                return;
            }

            //Then we have to extract the JWT token from the header
            //We remove "Bearer " from the string
            String token = header.substring(7);
            //Then we have to get the userName from the token ---
            //Refer to the getUserNamefromToken method in AuthUtil
            String username = authUtil.getUserNamefromToken(token);

            //If the username is not null and the SecurityContextHolder doesnt have an user ---- then we proceed to check for the user
            //After getting the user from the database we create a UsernamePasswordAuthenticationToken --- using the User object
            //We then set the SecurtiyContextHolder with the userToken created ----
            //Then we successfully go to the next filter
            if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null)  {
                User user = userRepository.findByEmail(username).orElseThrow();

                //For the authorities we set the user.getAuthorities with the authority --- the user has  ---
                //So that we can know about the user roles from the JWT and allow role based access --
                UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(userToken);
            }

            //If the next filter also sees the SecurityContextHolder has a userToken --- then it passes and goes on to the next Filter
            filterChain.doFilter(request,response);
        }
        catch(Exception e)  {
            //we send the request, response, handler and the exception objects
            handlerExceptionResolver.resolveException(request,response,null,e);
        }




    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/auth/")
                || path.startsWith("/register/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}