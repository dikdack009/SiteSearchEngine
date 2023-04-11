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


    public JwtResponse login(@NonNull JwtRequest authRequest){
        final Optional<User> optionalUser = userService.getByLogin(authRequest.getLogin());
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            if (!user.isEmailChecked()) {
                return new JwtResponse("Пользователь не подтверждён");
            }
        }
        else {
            return new JwtResponse("Пользователь не найден");
        }

        if (userService.validateUserPassword(authRequest.getLogin(), authRequest.getPassword())) {
            final String accessToken = jwtProvider.generateAccessToken(user);
            final String refreshToken = jwtProvider.generateRefreshToken(user);
            refreshStorage.put(user.getLogin(), refreshToken);
            return new JwtResponse(accessToken, refreshToken);
        } else {
            return new JwtResponse("Неверный пароль");
        }
    }

    public JwtResponse getAccessToken(@NonNull String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final Optional<User> optionalUser = userService.getByLogin(login);
                User user;
                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                }
                else {
                    return new JwtResponse("Пользователь не найден");
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
            final String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final Optional<User> optionalUser = userService.getByLogin(login);
                User user;
                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                }
                else {
                    return new JwtResponse("Пользователь не найден");
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

}
