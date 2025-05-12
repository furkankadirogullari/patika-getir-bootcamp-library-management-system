package tr.com.fkadirogullari.librarymanagementservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.Role;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final String SECRET_KEY = "Vv0fjNCFJut76RQkAKgqp2aJ0eUeCwqMd1G8N7Xp7KzqVEK8KdCVfPzqX3R1KZPvXaPbNMYefL69Fz8XGqZ9aA==";

    private final long EXPIRATION_TIME = 1000 * 60 * 60;

    // Method to generate a token
    public String generateToken(String email, Set<Role> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        // Convert roles set to a list of strings
        Set<String> roleStrings = roles.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        Map<String, Object> claims = new HashMap<>(); // Map to store claims (extra information)

        claims.put("roles", roleStrings);

        // Create and return the JWT token
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    // Method to get claims from the token
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    // Method to extract the email from the token
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Method to extract roles from the token
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        return (List<String>) claims.get("roles");
    }

    // Method to validate the token
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
