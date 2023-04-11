package pet.diploma.sitesearchengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import pet.diploma.sitesearchengine.model.Role;
import pet.diploma.sitesearchengine.model.User;
import pet.diploma.sitesearchengine.model.response.RegistrationResponse;
import pet.diploma.sitesearchengine.security.JwtRequest;
import pet.diploma.sitesearchengine.services.EmailService;
import pet.diploma.sitesearchengine.services.UserService;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Optional;

@Controller
public class RegistrationController {

    private final UserService userService;
    private final EmailService emailService;

    private final static String EMAIL_REGEX = "/^((([0-9A-Za-z]{1}[-0-9A-z\\.]{1,}[0-9A-Za-z]{1})|([0-9А-Яа-я]{1}[-0-9А-я\\.]" +
            "{1,}[0-9А-Яа-я]{1}))@([-A-Za-z]{1,}\\.){1,2}[-A-Za-z]{2,})$/u";

    @Autowired
    public RegistrationController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/api/registration")
    public ResponseEntity<RegistrationResponse> addUser(@RequestBody JwtRequest authRequest) throws MessagingException, UnsupportedEncodingException {
        User newUser = new User();
        if (checkFailedEmailFormat(authRequest.getLogin())) {
            return new ResponseEntity<>(new RegistrationResponse(false, "Неверный формат почты"), HttpStatus.BAD_REQUEST);
        }
        newUser.setLogin(authRequest.getLogin());
        newUser.setPassword(authRequest.getPassword());
        newUser.setRoles(Role.USER);
        newUser.setEmailChecked(false);
        RegistrationResponse registrationResponse;
        if (!userService.saveUser(newUser)){
            if (!userService.getByLogin(authRequest.getLogin()).get().isEmailChecked()) {
                return new ResponseEntity<>(new RegistrationResponse(true, null), HttpStatus.RESET_CONTENT);
            }
            registrationResponse = new RegistrationResponse(false, "Пользователь с такой почтой уже существует");
            return new ResponseEntity<>(registrationResponse, HttpStatus.BAD_REQUEST);
        }
        emailService.sendCheckCode(authRequest.getLogin());
        return new ResponseEntity<>(new RegistrationResponse(true, null), HttpStatus.OK);
    }

    @GetMapping("/api/verification/check")
    public ResponseEntity<RegistrationResponse> checkCode(@RequestParam String login, @RequestParam Integer code) {
        if (checkFailedEmailFormat(login)) {
            return new ResponseEntity<>(new RegistrationResponse(false, "Неверный формат почты"), HttpStatus.BAD_REQUEST);
        }
        if (code == null) {
            return new ResponseEntity<>(new RegistrationResponse(false, "Неверный формат кода подтверждения"), HttpStatus.BAD_REQUEST);
        }
        Optional<User> user = userService.getByLogin(login);
        if (user.isPresent()) {
            boolean checkCode = Objects.equals(emailService.getVerification().get(login), code);
            if (checkCode) {
                userService.updateCheckedUser(login);
            }
            return new ResponseEntity<>(new RegistrationResponse(checkCode, checkCode ? null :  "Неверный код"), checkCode ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new RegistrationResponse(false, "Неверный логин или пароль"), HttpStatus.NOT_FOUND);
    }

    private boolean checkFailedEmailFormat(String email) {
        return email.isEmpty() || !email.matches(EMAIL_REGEX);
    }
}
