package com.erp.oneflow.presentation.user.controller;

import com.erp.oneflow.application.user.userService.impl.UserServiceImpl;
import com.erp.oneflow.domain.user.userEntity.UserEntity;
import com.erp.oneflow.presentation.user.dto.login.UserLoginReq;
import com.erp.oneflow.presentation.user.dto.login.UserLoginRes;
import com.erp.oneflow.presentation.user.dto.register.UserRegisterReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserServiceImpl service;

    /**
     * [Register] 사용자 등록
     * @param userData
     * @param imageFile
     * @return
     */
    @PostMapping(value = "/register", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> userRegister(
            @RequestParam("userData") String userData,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UserRegisterReq userRegisterReq = objectMapper.readValue(userData, UserRegisterReq.class);

            log.info("Controller Password = {}", userRegisterReq.getPassword());

            service.userCreate(userRegisterReq.toUser(), imageFile);
            return ResponseEntity.ok("직원 등록 성공!");
        } catch (Exception e) {
            log.error("직원 등록 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("직원 등록 실패: " + e.getMessage());
        }
    }

    /**
     * [Login] 사용자 접속
     *
     * @param userLoginReq
     * @param response
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<String> userLogin(@RequestBody UserLoginReq userLoginReq, HttpServletResponse response) {
        log.info("Controller 로그인 요청");

        UserLoginRes loginRes = service.userLogin(userLoginReq.toLogin());

        Cookie tokenCookie = new Cookie("token", loginRes.getToken());
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(86400);
        tokenCookie.setAttribute("SameSite", "None");
        response.addCookie(tokenCookie);

        return ResponseEntity.ok("로그인 성공");
    }

    @PostMapping("/image-call")
    public ResponseEntity<?> userImageCall(@CookieValue(name = "token", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 없습니다.");
        }

        try {
            String profileImagePath = service.getUserProfileImage(token);
            return ResponseEntity.ok(Map.of("profileImage", profileImagePath));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie tokenCookie = new Cookie("token", null);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(0);

        response.addCookie(tokenCookie);
        return ResponseEntity.ok("로그아웃 성공");
    }


    /**
     * [Validate] Cookie 및 Token 유효성 검사
     *
     * @param request
     * @return
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validate(HttpServletRequest request) {

        log.info("토큰 검증 요청 : validate");

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("쿠키 없음");
        }

        for (Cookie cookie : cookies) {
            log.info("✅ 쿠키 확인: {} = {}", cookie.getName(), cookie.getValue());
        }

        String token = Arrays.stream(cookies)
                .filter(cookie -> "token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰 없음");
        }

        Map<String, Object> response = service.validateUser(token);
        return ResponseEntity.ok(response);
    }

}
