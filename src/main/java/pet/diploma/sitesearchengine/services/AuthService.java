package pet.diploma.sitesearchengine.services;

import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pet.diploma.sitesearchengine.model.Token;
import pet.diploma.sitesearchengine.repositories.TokensRepository;
import pet.diploma.sitesearchengine.security.JwtProvider;
import pet.diploma.sitesearchengine.model.User;
import pet.diploma.sitesearchengine.security.JwtAuthentication;
import pet.diploma.sitesearchengine.security.JwtRequest;
import pet.diploma.sitesearchengine.security.JwtResponse;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;
    private final TokensRepository tokensRepository;
    private final JwtProvider jwtProvider;
    private final static String EMAIL_REGEX = "([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\\.[a-zA-Z0-9_-]+)";


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
            saveToken(new Token(user.getLogin(), bCryptPasswordEncoder.encode(refreshToken)), bCryptPasswordEncoder.encode(refreshToken));
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
            final String saveRefreshToken = tokensRepository.getTokensByLogin(login).getRefreshToken();
            if (saveRefreshToken != null && bCryptPasswordEncoder.matches(refreshToken, saveRefreshToken)) {
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
        return new JwtResponse("Неверный refresh токен");
    }

    public JwtResponse refresh(@NonNull String refreshToken)  {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            if (checkFailedEmailFormat(login)) {
                return new JwtResponse("Неверный формат почты");
            }
            final String saveRefreshToken = tokensRepository.getTokensByLogin(login).getRefreshToken();
            if (saveRefreshToken != null && bCryptPasswordEncoder.matches(refreshToken, saveRefreshToken)) {
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
                saveToken(new Token(user.getLogin(), bCryptPasswordEncoder.encode(refreshToken)), bCryptPasswordEncoder.encode(refreshToken));
                return new JwtResponse(accessToken, newRefreshToken);
            }
        }
        return new JwtResponse("Невалидный JWT токен");
    }

    private void saveToken(Token token, String refresh) {
        Token tokenDB = tokensRepository.getTokensByLogin(token.getLogin());
        if (tokenDB != null) {
            tokensRepository.updateTokenByLogin(refresh, token.getLogin());
        } else {
            tokensRepository.save(token);
        }
    }

    public JwtAuthentication getAuthInfo() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean checkFailedEmailFormat(String email) {
        return email.isEmpty() || !email.matches(EMAIL_REGEX);
    }
}
