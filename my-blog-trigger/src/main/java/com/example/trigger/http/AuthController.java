package com.example.trigger.http;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.example.api.dto.LoginRequestDTO;
import com.example.api.dto.LoginResponseDTO;
import com.example.api.response.R;
import com.example.domain.auth.model.entity.SysUserEntity;
import com.example.domain.auth.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final IAuthService authService;

    @PostMapping("/login")
    public R<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        SysUserEntity entity = authService.handleLogin(requestDTO.getUsername(), requestDTO.getPassword());
        StpUtil.login(entity.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .tokenName(tokenInfo.getTokenName())
                .tokenValue(tokenInfo.getTokenValue())
                .build();
       return success("登录成功", responseDTO);
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        StpUtil.logout();
        return success("注销成功");
    }
}
