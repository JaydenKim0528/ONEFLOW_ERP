package com.erp.oneflow.infrastructure.util;

import com.erp.oneflow.application.user.userService.impl.SecretKeyService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    private final SecretKeyService secretKeyService;
    private Key key;
    private static final long EXPIRATION_TIME = 86400000L; // 24시간

    @PostConstruct
    public void init() {
        try {
            String secretKeyString = secretKeyService.getCurrentSecretKey();
            if (secretKeyString == null || secretKeyString.isEmpty()) {
                throw new IllegalStateException("[JwtUtil] Secret Key가 존재하지 않습니다.");
            }
            byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);
            this.key = Keys.hmacShaKeyFor(decodedKey);
            log.info("[JwtUtil] Secret Key 로딩 완료.");
        } catch (IOException e) {
            throw new RuntimeException("[JwtUtil] 시크릿 키를 불러오는 데 실패했습니다.", e);
        }
    }

    public String generateToken(Long userNo, String userId, String userName, String userEmail, String company, String userDepartment, String userPosition, String userRole) {
        if (key == null) {
            throw new IllegalStateException("[JwtUtil] Key가 초기화되지 않았습니다.");
        }

        try {
            return Jwts.builder()
                    .claim("userNo", userNo)
                    .claim("userId", userId)
                    .claim("userName", userName)
                    .claim("userEmail", userEmail)
                    .claim("company", company)
                    .claim("userDepartment", userDepartment)
                    .claim("userPosition", userPosition)
                    .claim("userRole", userRole)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            log.error("[JwtUtil] 토큰 생성 실패: {}", e.getMessage());
            throw new RuntimeException("[JwtUtil] 토큰 생성에 실패하였습니다.", e);
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("[JwtUtil] 제공된 토큰이 비어있습니다.");
            return false;
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JwtUtil] 만료된 토큰: {}", token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JwtUtil] 유효하지 않은 토큰: {}", token);
        }
        return false;
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key) // 서명 검증
                    .build()
                    .parseClaimsJws(token) // 토큰 파싱
                    .getBody(); // Claims 객체 반환

        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("토큰이 만료되었습니다.");
        } catch (UnsupportedJwtException e) {
            throw new IllegalArgumentException("지원되지 않는 JWT 형식입니다.");
        } catch (MalformedJwtException e) {
            throw new IllegalArgumentException("잘못된 JWT 형식입니다.");
        } catch (SignatureException e) {
            throw new IllegalArgumentException("JWT 서명 검증에 실패했습니다.");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("토큰 값이 유효하지 않습니다.");
        }
    }

    public Map<String, Object> getUserInfoFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("[JwtUtil] 제공된 토큰이 유효하지 않습니다.");
        }

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userNo", claims.get("userNo", Long.class));
        userInfo.put("userId", claims.get("userId", String.class));
        userInfo.put("userName", claims.get("userName", String.class));
        userInfo.put("userEmail", claims.get("userEmail", String.class));
        userInfo.put("company", claims.get("company", String.class));
        userInfo.put("userDepartment", claims.get("userDepartment", String.class));
        userInfo.put("userPosition", claims.get("userPosition", String.class));
        userInfo.put("userRole", claims.get("userRole", String.class));

        return userInfo;
    }
}
