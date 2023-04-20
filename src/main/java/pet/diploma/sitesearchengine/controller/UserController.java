package pet.diploma.sitesearchengine.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private final Logger rootLogger;
    private final static String EMAIL_REGEX = "([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\\.[a-zA-Z0-9_-]+)";
    private final static String PASSWORD_REGEX = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9@#$%]).{8,}";

    @Autowired
    public UserController(AuthService authService, UserService userService, EmailService emailService) {
        this.authService = authService;
        this.userService = userService;
        this.emailService = emailService;
        this.rootLogger = LogManager.getLogger("index");
    }

    @PostMapping("info")
    public ResponseEntity<InfoResponse> getLoginByToken() {
        try {
            String login = authService.getAuthInfo().getPrincipal().toString();
            Optional<User> optionalUser = userService.getByLogin(login);
            return optionalUser.map(user -> ResponseEntity.ok(new InfoResponse(login, user.isNotify(), null)))
                    .orElseGet(() -> new ResponseEntity<>(new InfoResponse(null, null, "Пользователь не найден"), HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(new InfoResponse(null, null, "Пользователь не найден"), HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("change")
    public ResponseEntity<JwtResponse> changePassword(@RequestBody @NotNull JwtChangeRequest authRequest) {
        rootLogger.info(authRequest.getLogin() + ":\tПользователь меняет пароль");
        final JwtResponse token = authService.login(new JwtRequest(authRequest.getLogin(), authRequest.getPassword()));
        Optional<User> optionalUser = userService.getByLogin(authRequest.getLogin());
        if (token.getError() == null && optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword(authRequest.getNewPassword());
            userService.updateUserPasswordByLogin(user);
            rootLogger.info(authRequest.getLogin() + ":\tПользователь успешно изменён");
            return new ResponseEntity<>(token, HttpStatus.OK);
        }
        if (token.getError() != null) {
            rootLogger.error(authRequest.getLogin() + ":\tПароль не изменён: Неверный старый пароль");
            return new ResponseEntity<>(new JwtResponse("Неверный старый пароль"), HttpStatus.FORBIDDEN);
        }  else {
            rootLogger.error(authRequest.getLogin() + ":\tПароль не изменён: Пользователь не найден");
            return new ResponseEntity<>(new JwtResponse("Пользователь не найден"), HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping ("recover")
    public ResponseEntity<RegistrationResponse> recoverPassword(@RequestBody @NotNull RecoverRequest authRequest) {
        rootLogger.info(authRequest.getLogin() + ":\tПользователь восстанавливает пароль");
        Optional<User> optionalUser = userService.getByLogin(authRequest.getLogin());
        int codeNumber;
        if (checkFailedEmailFormat(authRequest.getLogin().trim())) {
            rootLogger.error(authRequest.getLogin() + ":\tОшибка восстановления пароля: Неверный формат почты");
            return new ResponseEntity<>(new RegistrationResponse(false, "Неверный формат почты"), HttpStatus.BAD_REQUEST);
        }
        if (checkFailedPasswordFormat(authRequest.getPassword())) {
            rootLogger.error(authRequest.getLogin() + ":\tОшибка восстановления пароля: Неверный формат пароля");
            return new ResponseEntity<>(new RegistrationResponse(false, "Неверный формат пароля"), HttpStatus.BAD_REQUEST);
        }
        if (optionalUser.isPresent()) {
            try {
                codeNumber = Integer.parseInt(authRequest.getCode().trim());
                if ((codeNumber > 999999 || codeNumber < 100000)) {
                    rootLogger.error(authRequest.getLogin() + ":\tОшибка восстановления пароля: Неверный формат кода подтверждения");
                    return new ResponseEntity<>(new RegistrationResponse(false, "Неверный формат кода подтверждения"), HttpStatus.BAD_REQUEST);
                }
            } catch (NumberFormatException e) {
                rootLogger.error(authRequest.getLogin() + ":\tОшибка восстановления пароля: Неверный формат кода подтверждения");
                return new ResponseEntity<>(new RegistrationResponse(false, "Неверный формат кода подтверждения"), HttpStatus.BAD_REQUEST);
            }
            if (Objects.equals(emailService.getRecover().get(authRequest.getLogin()), codeNumber)) {
                emailService.getRecover().remove(authRequest.getLogin());
                User user = optionalUser.get();
                user.setPassword(authRequest.getPassword());
                userService.updateUserPasswordByLogin(user);
                return new ResponseEntity<>(new RegistrationResponse(true, null), HttpStatus.OK);
            } else {
                rootLogger.error(authRequest.getLogin() + ":\tОшибка восстановления пароля: Неверный код");
                return new ResponseEntity<>(new RegistrationResponse(false, "Неверный код"), HttpStatus.BAD_REQUEST);
            }
        } else {
            rootLogger.error(authRequest.getLogin() + ":\tОшибка восстановления пароля: Пользователь не найден");
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
            rootLogger.info(notifyRequest.getLogin() + ":\tСтатус уведомлений изменён");
            return new ResponseEntity<>(new RegistrationResponse(true, null), HttpStatus.OK);
        } else {
            rootLogger.error(notifyRequest.getLogin() + ":\tСтатус не изменён: Пользователь не найден");
            return new ResponseEntity<>(new RegistrationResponse(true,"Пользователь не найден"), HttpStatus.NOT_FOUND);
        }
    }

    private boolean checkFailedEmailFormat(String email) {
        return email.isEmpty() || !email.matches(EMAIL_REGEX);
    }

    private boolean checkFailedPasswordFormat(String password) {
        return password.isEmpty() || !password.matches(PASSWORD_REGEX);
    }
}
