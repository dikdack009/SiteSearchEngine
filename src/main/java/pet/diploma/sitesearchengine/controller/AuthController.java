package pet.diploma.sitesearchengine.controller;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pet.diploma.sitesearchengine.services.AuthService;
import pet.diploma.sitesearchengine.security.JwtRequest;
import pet.diploma.sitesearchengine.security.JwtResponse;
import pet.diploma.sitesearchengine.security.RefreshJwtRequest;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("login")
    public ResponseEntity<JwtResponse> login(@RequestBody @NotNull JwtRequest authRequest) {
        System.out.println("Пользователь " + authRequest.getLogin()  + " аутентифицируется в системе");
        final JwtResponse token = authService.login(authRequest);
        return token.getError() == null ? ResponseEntity.ok(token) : new ResponseEntity<>(token, HttpStatus.FORBIDDEN);
    }

    @PostMapping("token")
    public ResponseEntity<JwtResponse> getNewAccessToken(@RequestBody @NotNull RefreshJwtRequest request) {
        final JwtResponse token = authService.getAccessToken(request.getRefreshToken());
        return token.getError() == null ? ResponseEntity.ok(token) : new ResponseEntity<>(token, HttpStatus.FORBIDDEN);
    }

    @PostMapping("refresh")
    public ResponseEntity<JwtResponse> getNewRefreshToken(@RequestBody @NotNull RefreshJwtRequest request) {
        final JwtResponse token = authService.refresh(request.getRefreshToken());
        return token.getError() == null ? ResponseEntity.ok(token) : new ResponseEntity<>(token, HttpStatus.FORBIDDEN);
    }

    @PostMapping("info")
    public ResponseEntity<String> getLoginByToken(@RequestBody @NotNull JwtRequest authRequest) {
        return ResponseEntity.ok(authRequest.getLogin());
//        TODO: Проверить при неправильных токенах
    }
}