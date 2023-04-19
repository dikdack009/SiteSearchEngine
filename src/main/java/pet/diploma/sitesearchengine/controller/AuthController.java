package pet.diploma.sitesearchengine.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pet.diploma.sitesearchengine.services.AuthService;
import pet.diploma.sitesearchengine.security.JwtRequest;
import pet.diploma.sitesearchengine.security.JwtResponse;
import pet.diploma.sitesearchengine.security.RefreshJwtRequest;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;
    private final Logger rootLogger;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
        this.rootLogger = LogManager.getRootLogger();
    }

    @PostMapping("login")
    public ResponseEntity<JwtResponse> login(@RequestBody @NotNull JwtRequest authRequest) {
        rootLogger.info(authRequest.getLogin() + ":\tПользователь аутентифицируется в системе");
        final JwtResponse token = authService.login(authRequest);
        return answer(authRequest.getLogin(), token);
    }

    @PostMapping("token")
    public ResponseEntity<JwtResponse> getNewAccessToken(@RequestBody @NotNull RefreshJwtRequest request) {
        String email = "";
        rootLogger.info(email + ":\tПользователь получает новый access токен");
        final JwtResponse token = authService.getAccessToken(request.getRefreshToken());
        return answer(email, token);
    }

    @PostMapping("refresh")
    public ResponseEntity<JwtResponse> getNewRefreshToken(@RequestBody @NotNull RefreshJwtRequest request) {
        String email = "";
        rootLogger.info(email + ":\tПользователь получает новый refresh токен");
        final JwtResponse token = authService.refresh(request.getRefreshToken());
        return answer(email, token);
    }

    private ResponseEntity<JwtResponse> answer(String email, JwtResponse token) {
        if (token.getError() == null) {
            rootLogger.info(email + ":\tУспешно");
            return ResponseEntity.ok(token);
        } else {
            rootLogger.error(email + ":\tНеуспешно - " + token.getError());
            return new ResponseEntity<>(token, HttpStatus.FORBIDDEN);
        }
    }
}