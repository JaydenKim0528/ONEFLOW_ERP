package com.erp.oneflow.presentation.user.controller;

import com.erp.oneflow.application.user.userService.impl.UserServiceImpl;
import com.erp.oneflow.presentation.user.dto.login.UserLoginReq;
import com.erp.oneflow.presentation.user.dto.login.UserLoginRes;
import com.erp.oneflow.presentation.user.dto.register.UserRegisterReq;
import com.fasterxml.jackson.databind.ObjectMapper;
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
     *
     * @param imageFile
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
    public ResponseEntity<Map<String, Object>> userLogin(@RequestBody @Valid UserLoginReq userLoginReq, HttpServletResponse response) {
        log.info("Controller 로그인 요청");

        UserLoginRes loginRes = service.userLogin(userLoginReq.toLogin());

        Cookie tokenCookie = new Cookie("token", loginRes.getToken());
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(86400);
        response.addCookie(tokenCookie);

        log.info("쿠키 체크 : tokenCookie={}", tokenCookie);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("userNo", loginRes.getUserNo());
        responseBody.put("userId", loginRes.getUserId());
        responseBody.put("userName", loginRes.getUserName());
        responseBody.put("userDepartment", loginRes.getUserDepartment());
        responseBody.put("userPosition", loginRes.getUserPosition());
        responseBody.put("role", loginRes.getUserRole());

        log.info("사용자 번호 : userNo={}", loginRes.getUserNo());
        log.info("사용자 계정 : userId={}", loginRes.getUserId());
        log.info("사용자 이름 : userName={}", loginRes.getUserName());
        log.info("사용자 부서 : userName={}", loginRes.getUserDepartment());
        log.info("사용자 직급 : userName={}", loginRes.getUserPosition());
        log.info("사용자 권한 : role={}", loginRes.getUserRole());

        return ResponseEntity.ok(responseBody);
    }

    /**
     * [TokenValid] 토큰 유효성 검증
     *
     * @param request
     * @return
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validate(HttpServletRequest request) {
        String token = Arrays.stream(request.getCookies())
                .filter(cookie -> "token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        Map<String, Object> response = service.validateUser(token);
        return ResponseEntity.ok(response);
    }
}
