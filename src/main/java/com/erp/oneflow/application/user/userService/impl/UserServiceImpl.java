package com.erp.oneflow.application.user.userService.impl;

import com.erp.oneflow.application.user.userService.UserService;
import com.erp.oneflow.domain.user.userEntity.UserEntity;
import com.erp.oneflow.domain.user.userModel.UserInfo;
import com.erp.oneflow.infrastructure.repository.UserRepository;
import com.erp.oneflow.infrastructure.util.JwtUtil;
import com.erp.oneflow.presentation.user.dto.login.UserLoginRes;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${file.upload.image-dir}")
    private String imageDir;


    /**
     * [Register] 사용자 정보 등록 프로세스
     *
     * @param userInfo
     * @param imageFile
     * @return
     */
    @Transactional
    @Override
    public UserInfo userCreate(final UserInfo userInfo, MultipartFile imageFile) throws FileUploadException {

        log.info("Service Password = {}", userInfo.getPassword());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userInfo.getPassword());

        // Image 저장 및 정보(Name, Path) 생성
        String userImageName = generateUniqueFileName(imageFile.getOriginalFilename());
        String userImagePath = saveFile(imageFile, userImageName)
                .orElseThrow(() -> new RuntimeException("이미지 저장 실패"));

        // UserInfo 정보 업데이트(Password, ImageName, ImagePath, CreateAt)
        UserInfo updatedUserInfo = userInfo.toBuilder()
                .password(encodedPassword)
                .userImageName(userImageName)
                .userImagePath(userImagePath)
                .userCreatedAt(LocalDateTime.now())
                .build();

        // UserEntity 정보 저장
        UserEntity userEntity = updatedUserInfo.toRegister();

        // DB 저장
        UserEntity savedEntity = userRepository.save(userEntity);

        return UserInfo.from(savedEntity);
    }

    /**
     * [Login] 사용자 접속 프로세스
     *
     * @param userInfo
     * @return
     */
    @Override
    public UserLoginRes userLogin(final UserInfo userInfo) {
        log.info("로그인 요청: userId={}, password={}", userInfo.getUserId(), userInfo.getPassword());

        UserEntity entity = userRepository.findByUserId(userInfo.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserInfo found = UserInfo.from(entity);
        log.info("데이터베이스에서 조회된 사용자: {}", found);

        if (!passwordEncoder.matches(userInfo.getPassword(), found.getPassword())) {
            log.warn("비밀번호 불일치: 입력된 비밀번호={}", userInfo.getPassword());
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        log.info("로그인 성공: userId={}", found.getUserId());
        String token = jwtUtil.generateToken(found.getUserNo(), found.getUserId(), found.getUserName(), found.getUserEmail(), found.getCompany(), found.getUserDepartment(), found.getUserPosition(), found.getUserRole());

        return UserLoginRes.builder()
                .userNo(found.getUserNo())
                .userId(found.getUserId())
                .userName(found.getUserName())
                .userEmail(found.getUserEmail())
                .company(found.getCompany())
                .userDepartment(found.getUserDepartment())
                .userPosition(found.getUserPosition())
                .userRole(found.getUserRole())
                .token(token)
                .build();
    }

    /**
     * [Profile] 사용자 이미지 요청
     *
     * @param token
     * @return
     */
    @Override
    public String getUserProfileImage(String token) {
        log.info("사용자 이미지 요청");

        Claims claims = jwtUtil.parseToken(token);
        String userId = claims.get("userId", String.class);

        log.info("✅ 추출된 userId: {}", userId);

        UserEntity entity = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String imagePath = entity.getUserImagePath();
        String imageName = entity.getUserImageName();

        log.info("imageName: {}", imageName);
        log.info("imagePath: {}", imagePath);

        if (imagePath != null && imageName != null) {
            return "/static/userImage/" + imageName;
        }

        return "/static/userImage/default-profile.png";
    }

    /**
     * [Valid] 사용자 토근 유효성 검증
     *
     * @param token
     * @return
     */
    @Override
    public Map<String, Object> validateUser(String token) {
        Map<String, Object> response = new HashMap<>();
            if (token != null && jwtUtil.validateToken(token)) {
                Map<String, ?> userInfo = jwtUtil.getUserInfoFromToken(token);
                response.put("loggedIn", true);
                response.put("userNo", userInfo.get("userNo"));
                response.put("userId", userInfo.get("userId"));
                response.put("userName", userInfo.get("userName"));
                response.put("userEmail", userInfo.get("userEmail"));
                response.put("company", userInfo.get("company"));
                response.put("userDepartment", userInfo.get("userDepartment"));
                response.put("userPosition", userInfo.get("userPosition"));
                response.put("userRole", userInfo.get("userRole"));
            } else {
                response.put("loggedIn", false);
            }
        return response;
    }

    /**
     * [SavaFile] 사용자 이미지 파일 등록
     *
     * @param file
     * @param fileName
     * @return
     */
    private Optional<String> saveFile(MultipartFile file, String fileName) throws FileUploadException {
        // File 유효성 검사
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("파일이 제공되지 않았습니다.");
        }

        // Directory 검토 및 File 저장
        try {
            File directory = new File(imageDir);
            if(!directory.exists() && !directory.mkdirs()) {
                throw new FileUploadException("디렉토리 생성 실패");
            }
            File destinationFile = new File(directory, fileName);
            file.transferTo(destinationFile);
            log.info("파일 저장 완료 : {}", destinationFile.getAbsolutePath());
            return Optional.of(destinationFile.getAbsolutePath());
        } catch (IOException e) {
            throw new FileUploadException("파일 저장 실패: " + e.getMessage());
        }
    }

    /**
     * [FileName] 사용자 이미지 파일명 생성
     *
     * @param originalFileName
     * @return
     */
    private String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "_" + originalFileName;
    }

}
