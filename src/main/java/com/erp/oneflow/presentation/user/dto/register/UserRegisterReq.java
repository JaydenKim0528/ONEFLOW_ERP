package com.erp.oneflow.presentation.user.dto.register;

import com.erp.oneflow.domain.user.userModel.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterReq {

    private String userId;                                       // 계정
    private String password;                                     // 비밀번호
    private String userName;                                     // 이름
    private String userEmail;                                    // 이메일
    private String company;                                      // 회사
    private String userDepartment;                               // 부서
    private String userPosition;                                 // 직급
    private String userPhone;                                    // 연락처
    private String userRole;                                     // 권한
    private LocalDateTime userCreatedAt;                         // 생성일

    public UserInfo toUser() {
        return UserInfo.builder()
                .userId(userId)
                .password(password)
                .userName(userName)
                .userEmail(userEmail)
                .company(company)
                .userDepartment(userDepartment)
                .userPosition(userPosition)
                .userPhone(userPhone)
                .userRole(userRole)
                .userCreatedAt(userCreatedAt)
                .build();
    }
}
