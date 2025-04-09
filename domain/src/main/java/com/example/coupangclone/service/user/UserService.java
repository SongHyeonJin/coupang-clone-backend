package com.example.coupangclone.service.user;

import com.example.coupangclone.auth.JwtPort;
import com.example.coupangclone.auth.RedisPort;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.entity.user.command.LoginCommand;
import com.example.coupangclone.entity.user.command.SignupCommand;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.repository.user.UserRepository;
import com.example.coupangclone.result.LoginResult;
import com.example.coupangclone.result.SignupResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtPort jwtPort;
    private final RedisPort redisPort;
    private final TokenService tokenService;

    @Transactional
    public SignupResult signup(SignupCommand command) {
        String email = command.email();
        String password = passwordEncoder.encode(command.password());
        String name = command.name();
        String tel = command.tel();
        String gender = command.gender();

        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw  new ErrorException(ExceptionEnum.EMAIL_DUPLICATION);
        }

        User user = User.builder()
                .email(email)
                .password(password)
                .name(name)
                .tel(tel)
                .gender(gender)
                .role(UserRoleEnum.USER)
                .build();
        userRepository.save(user);
        return new SignupResult(name);
    }

    @Transactional
    public LoginResult login(LoginCommand command) {
        String email = command.email();
        String password = command.password();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ErrorException(ExceptionEnum.USER_NOT_FOUND)
        );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ErrorException(ExceptionEnum.WRONG_PASSWORD);
        }

        String accessToken = jwtPort.createAccessToken(user.getId(), user.getEmail(), user.getName(), user.getRole());
        String refreshToken = jwtPort.createRefreshToken(user.getId());

        redisPort.set("RT:" + user.getId(), refreshToken, 14, TimeUnit.DAYS);;

        return new LoginResult(user.getName(), accessToken, refreshToken);
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        tokenService.logout(accessToken, refreshToken);
    }

}
