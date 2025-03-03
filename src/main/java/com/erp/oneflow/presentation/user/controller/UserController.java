package com.erp.oneflow.presentation.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/test")
    public String testApi() {
        return "백엔드 연결 성공";
    }

}
