package com.Spring.Multi_Role_Job_Portal.Security;

import com.Spring.Multi_Role_Job_Portal.Entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthUtil {


    //We have the jwt secretKey first ---- we have defined it in application.properties ---
    //We inject it using the Value annotaiton which takes the variable name and injects it accordingly ---
    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    //    This function creates and returns a cryptographic secret key that is typically used for signing and verifying JWTs (JSON Web Tokens) using HMAC algorithms like HS256.
    private SecretKey getSecretKey()    {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    //This finally generates the JWT token needed from the user credentials ----
    //Using the Jwts we first build then take the user payload --- (username) ---- to build and takes the secretKey to build --- signWith ---
    //We also define the issueAt(current Date) and the expiryTime(current Time + 10 mins)
    //Then we also define the userId issued ----
    public String generateAccessToken(User user)    {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId" , user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000*60*10))
                .signWith(getSecretKey())
                .compact();
    }


    //Here we parse the Username from the JWT Token ---
    //First we create a JWt Parser by verifying it with the secretKey and then build it
    //Then using the parser we Parse Signed Claims inside the token ---- see the upper method --- we have store the userId and the username inside the signed Claims user the payload part ---- we get the payload after parsing --- This returns an object of Claims type --- This object contains both the userId and username ---
    //At last we return the Username ---- getSubject() --- as the username was stored inside the Subject ----
    public String getUserNamefromToken(String token) {
        Claims claim =  Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claim.getSubject();
    }
}
