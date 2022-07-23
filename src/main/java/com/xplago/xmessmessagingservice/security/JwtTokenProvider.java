package com.xplago.xmessmessagingservice.security;

import com.xplago.xmessmessagingservice.exception.CustomException;
import com.xplago.xmessmessagingservice.model.user.XMessUserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        List<XMessUserRole> roles = Arrays.stream(claims.get("auth").toString().split(" ")).map(XMessUserRole::valueOf).toList();
        UserDetails userDetails = User
                .withUsername(claims.getSubject())
                .password("")
                .authorities(roles)
                .accountExpired(Boolean.parseBoolean(claims.get("accountExpired").toString()))
                .accountLocked(Boolean.parseBoolean(claims.get("accountLocked").toString()))
                .credentialsExpired(Boolean.parseBoolean(claims.get("credentialsExpired").toString()))
                .disabled(Boolean.parseBoolean(claims.get("disabled").toString()))
                .build();
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer "))
            return bearerToken.substring(7);
        else
            return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new CustomException("Expired or invalid JWT token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
