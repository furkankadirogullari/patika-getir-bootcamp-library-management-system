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

    public String generateToken(String email, Set<Role> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        Set<String> roleStrings = roles.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        Map<String, Object> claims = new HashMap<>();

        claims.put("roles", roleStrings);
        //roles.stream().map(Enum::name).collect(Collectors.toList())

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        return (List<String>) claims.get("roles");
    }
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
