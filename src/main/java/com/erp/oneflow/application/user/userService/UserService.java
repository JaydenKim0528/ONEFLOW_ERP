package com.erp.oneflow.application.user.userService;

import com.erp.oneflow.domain.user.userModel.UserInfo;
import com.erp.oneflow.presentation.user.dto.login.UserLoginRes;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserService {

    UserInfo userCreate(final UserInfo userInfo, MultipartFile imageFile) throws FileUploadException;

    UserLoginRes userLogin(final UserInfo userInfo);

    Map<String, Object> validateUser(String token);

}
