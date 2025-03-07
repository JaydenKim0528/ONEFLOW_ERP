package com.erp.oneflow.domain.user.userModel;

import com.erp.oneflow.domain.user.userEntity.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Builder(toBuilder = true)
public class UserInfo {

    private Long userNo;
    private String userId;
    private String password;
    private String userName;
    private String userEmail;
    private String userImageName;
    private String userImagePath;
    private String company;
    private String userDepartment;
    private String userPosition;
    private String userPhone;
    private String userRole;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdateAt;
    private LocalDateTime userDeleteAt;

    public static UserInfo from(UserEntity userEntity) {
        return UserInfo.builder()
                .userNo(userEntity.getUserNo())
                .userId(userEntity.getUserId())
                .password(userEntity.getPassword())
                .userName(userEntity.getUserName())
                .userEmail(userEntity.getUserEmail())
                .userImageName(userEntity.getUserImageName())
                .userImagePath(userEntity.getUserImagePath())
                .company(userEntity.getCompany())
                .userDepartment(userEntity.getUserDepartment())
                .userPosition(userEntity.getUserPosition())
                .userPhone(userEntity.getUserPhone())
                .userRole(userEntity.getUserRole())
                .userCreatedAt(userEntity.getUserCreatedAt())
                .userUpdateAt(userEntity.getUserUpdateAt())
                .userDeleteAt(userEntity.getUserDeleteAt())
                .build();
    }

    public UserEntity toRegister() {
        return UserEntity.builder()
                .userId(userId)
                .password(password)
                .userName(userName)
                .userEmail(userEmail)
                .userImageName(userImageName)
                .userImagePath(userImagePath)
                .company(company)
                .userDepartment(userDepartment)
                .userPosition(userPosition)
                .userPhone(userPhone)
                .userRole(userRole)
                .userCreatedAt(userCreatedAt)
                .build();
    }


}
