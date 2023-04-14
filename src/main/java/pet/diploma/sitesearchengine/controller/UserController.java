package pet.diploma.sitesearchengine.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pet.diploma.sitesearchengine.model.User;
import pet.diploma.sitesearchengine.model.response.InfoResponse;
import pet.diploma.sitesearchengine.model.response.NotifyRequest;
import pet.diploma.sitesearchengine.model.response.RecoverRequest;
import pet.diploma.sitesearchengine.model.response.RegistrationResponse;
import pet.diploma.sitesearchengine.security.JwtChangeRequest;
import pet.diploma.sitesearchengine.security.JwtRequest;
import pet.diploma.sitesearchengine.security.JwtResponse;
import pet.diploma.sitesearchengine.services.AuthService;
import pet.diploma.sitesearchengine.services.EmailService;
import pet.diploma.sitesearchengine.services.UserService;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("api/user")
public class UserController {
    private final AuthService authService;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public UserController(AuthService authService, UserService userService, EmailService emailService) {
        this.authService = authService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("info")
    public ResponseEntity<InfoResponse> getLoginByToken() {
        try {
            String login = authService.getAuthInfo().getPrincipal().toString();
            System.out.println("Login - " + login);
            Optional<User> optionalUser = userService.getByLogin(login);
            return optionalUser.map(user -> ResponseEntity.ok(new InfoResponse(login, user.isNotify(), null)))
                    .orElseGet(() -> new ResponseEntity<>(new InfoResponse(null, null, "Пользователь не найден"), HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(new InfoResponse(null, null, "Пользователь не найден"), HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("change")
    public ResponseEntity<JwtResponse> changePassword(@RequestBody @NotNull JwtChangeRequest authRequest) {
        final JwtResponse token = authService.login(new JwtRequest(authRequest.getLogin(), authRequest.getPassword()));
        Optional<User> optionalUser = userService.getByLogin(authRequest.getLogin());
        if (token.getError() == null && optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword(authRequest.getNewPassword());
            userService.updateUserPasswordByLogin(user);
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new JwtResponse("Пользователь не найден"), HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping ("recover")
    public ResponseEntity<RegistrationResponse> recoverPassword(@RequestBody @NotNull RecoverRequest authRequest) {
        Optional<User> optionalUser = userService.getByLogin(authRequest.getLogin());
        int codeNumber;
        try {
            codeNumber = Integer.parseInt(authRequest.getCode().trim());
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(new RegistrationResponse(false, "Неверный формат кода подтверждения"), HttpStatus.BAD_REQUEST);
        }
        if (optionalUser.isPresent()) {
            if ((codeNumber > 999999 || codeNumber < 100000)) {
                return new ResponseEntity<>(new RegistrationResponse(false, "Неверный формат кода подтверждения"), HttpStatus.BAD_REQUEST);
            }
            if (Objects.equals(emailService.getRecover().get(authRequest.getLogin()), codeNumber)) {
                emailService.getRecover().remove(authRequest.getLogin());
                User user = optionalUser.get();
                user.setPassword(authRequest.getPassword());
                userService.updateUserPasswordByLogin(user);
                return new ResponseEntity<>(new RegistrationResponse(true, null), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new RegistrationResponse(false, "Неверный код"), HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(new RegistrationResponse(true,"Пользователь не найден"), HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/notify")
    public ResponseEntity<RegistrationResponse> changeNotification(@NotNull @RequestBody NotifyRequest notifyRequest) {
        Optional<User> optionalUser = userService.getByLogin(notifyRequest.getLogin());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setNotify(notifyRequest.isFlag());
            userService.updateUserNotifyByLogin(user);
            return new ResponseEntity<>(new RegistrationResponse(true, null), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new RegistrationResponse(true,"Пользователь не найден"), HttpStatus.NOT_FOUND);
        }
    }
}
