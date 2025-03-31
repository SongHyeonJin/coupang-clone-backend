package com.example.coupangclone.user.controller;

import com.example.coupangclone.auth.jwt.JwtProvider;
import com.example.coupangclone.global.exception.GlobalExceptionHandler;
import com.example.coupangclone.user.dto.SignupRequestDto;
import com.example.coupangclone.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, UserControllerTest.DummyConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class DummyConfig {
        @Bean
        public JwtProvider jwtProvider() {
            return Mockito.mock(JwtProvider.class);
        }
        @Bean
        UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    @DisplayName("이메일을 입력하지 않으면 회원가입에 실패한다.")
    @Test
    void signup_validation_fail_email_blank() throws Exception {
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("")
                .password("qwer123!")
                .name("홍길동")
                .tel("01012345678")
                .gender("남성")
                .build();

        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value(
                        containsString("E-mail을 입력해주세요") ));
    }

    @DisplayName("유효하지 않은 이메일 형식을 입력하면 회원가입에 실패한다.")
    @Test
    void signup_validation_fail_wrong_emailPattern() throws Exception {
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("test@exam")
                .password("qwer123!")
                .name("홍길동")
                .tel("01012345678")
                .gender("남성")
                .build();

        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value(
                        containsString("유효한 이메일 주소를 입력해주세요") ));
    }

    @DisplayName("비밀번호를 입력하지 않으면 회원가입에 실패한다.")
    @Test
    void signup_validation_fail_password_blank() throws Exception {
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("test@example.com")
                .password("")
                .name("홍길동")
                .tel("01012345678")
                .gender("남성")
                .build();

        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value(
                        containsString("비밀번호를 입력해주세요") ));
    }

    @DisplayName("비밀번호가 최소 8자 이상 15자 이하가 아니면 회원가입에 실패한다.")
    @Test
    void signup_validation_fail_wrong_password_size() throws Exception {
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("test@example.com")
                .password("qr12!")
                .name("홍길동")
                .tel("01012345678")
                .gender("남성")
                .build();

        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value(
                        containsString("비밀번호는 최소 8자에서 15자 사이로만 가능합니다") ));
    }

    @DisplayName("비밀번호가 대소문자, 숫자, 특수문자를 포함하지 않으면 회원가입에 실패한다.")
    @Test
    void signup_validation_fail_wrong_passwordPattern() throws Exception {
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .email("test@example.com")
                .password("qwer12345")
                .name("홍길동")
                .tel("01012345678")
                .gender("남성")
                .build();

        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value(
                        containsString("비밀번호는 대소문자, 숫자, 특수문자를 포함해야됩니다") ));
    }

}