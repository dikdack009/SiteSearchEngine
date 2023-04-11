package pet.diploma.sitesearchengine.services;

import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pet.diploma.sitesearchengine.security.JwtProvider;
import pet.diploma.sitesearchengine.model.User;
import pet.diploma.sitesearchengine.security.JwtAuthentication;
import pet.diploma.sitesearchengine.security.JwtRequest;
import pet.diploma.sitesearchengine.security.JwtResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final Map<String, String> refreshStorage = new HashMap<>();
    private final JwtProvider jwtProvider;
    private final static String EMAIL_REGEX = "/^((([0-9A-Za-z]{1}[-0-9A-z\\.]{1,}[0-9A-Za-z]{1})|([0-9А-Яа-я]{1}[-0-9А-я\\.]" +
            "{1,}[0-9А-Яа-я]{1}))@([-A-Za-z]{1,}\\.){1,2}[-A-Za-z]{2,})$/u";


    public JwtResponse login(@NonNull JwtRequest authRequest){
        if (checkFailedEmailFormat(authRequest.getLogin())) {
            return new JwtResponse("Неверный формат почты");
        }
        final Optional<User> optionalUser = userService.getByLogin(authRequest.getLogin());
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        }
        else {
            return new JwtResponse("Неверный логин или пароль");
        }

        if (userService.validateUserPassword(authRequest.getLogin(), authRequest.getPassword())) {
            if (!user.isEmailChecked()) {
                return new JwtResponse("Пользователь не подтверждён");
            }
            final String accessToken = jwtProvider.generateAccessToken(user);
            final String refreshToken = jwtProvider.generateRefreshToken(user);
            refreshStorage.put(user.getLogin(), refreshToken);
            return new JwtResponse(accessToken, refreshToken);
        } else {
            return new JwtResponse("Неверный логин или пароль");
        }

    }

    public JwtResponse getAccessToken(@NonNull String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            if (checkFailedEmailFormat(login)) {
                return new JwtResponse("Неверный формат почты");
            }
            final String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final Optional<User> optionalUser = userService.getByLogin(login);
                User user;
                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                }
                else {
                    return new JwtResponse("Неверный логин или пароль");
                }
                final String accessToken = jwtProvider.generateAccessToken(user);
                return new JwtResponse(accessToken, null);
            }
        }
        return new JwtResponse(null, null);
    }

    public JwtResponse refresh(@NonNull String refreshToken)  {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            if (checkFailedEmailFormat(login)) {
                return new JwtResponse("Неверный формат почты");
            }
            final String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final Optional<User> optionalUser = userService.getByLogin(login);
                User user;
                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                }
                else {
                    return new JwtResponse("Неверный логин или пароль");
                }
                final String accessToken = jwtProvider.generateAccessToken(user);
                final String newRefreshToken = jwtProvider.generateRefreshToken(user);
                refreshStorage.put(user.getLogin(), newRefreshToken);
                return new JwtResponse(accessToken, newRefreshToken);
            }
        }
        return new JwtResponse("Невалидный JWT токен");
    }

    public JwtAuthentication getAuthInfo() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean checkFailedEmailFormat(String email) {
        return email.isEmpty() || !email.matches(EMAIL_REGEX);
    }
}
