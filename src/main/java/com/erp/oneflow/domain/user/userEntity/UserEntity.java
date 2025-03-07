package com.erp.oneflow.domain.user.userEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "EMPLOYEE")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userNo;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = true)
    private String userImageName;

    @Column(nullable = true)
    private String userImagePath;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String userDepartment;

    @Column(nullable = false)
    private String userPosition;

    @Column(nullable = true)
    private String userPhone;

    @Column(nullable = false)
    private String userRole;

    @Column(nullable = false)
    private LocalDateTime userCreatedAt;

    @Column(nullable = true)
    private LocalDateTime userUpdateAt;

    @Column(nullable = true)
    private LocalDateTime userDeleteAt;

}
