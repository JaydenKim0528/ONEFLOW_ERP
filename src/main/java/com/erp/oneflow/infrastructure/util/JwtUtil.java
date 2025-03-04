package com.erp.oneflow.infrastructure.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Key key = ;
    private static final long EXPIRATION_TIME = 86400000;

    public static String generateToken(Long userNo, String userId, String userName, String role) {

        try {
            return Jwts.builder()
                    .claim("userNo", userNo)
                    .claim("userId", userId)
                    .claim("userName", userName)
                    .claim("role", role)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("토큰 생성을 실패하였습니다.", e);
        }
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJwt(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public static Map<String, ?> getUserInfoFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJwt(token)
                .getBody();

        Long userNo = claims.get("userNo", Long.class);
        String userId = claims.get("userId", String.class);
        String userName = claims.get("userName", String.class);
        String role = claims.get("role", String.class);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userNo", userNo);
        userInfo.put("userId", userId);
        userInfo.put("userName", userName); // userName 추가
        userInfo.put("role", role);

        return userInfo;
    }


}
