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
import pet.diploma.sitesearchengine.security.JwtRequest;
import pet.diploma.sitesearchengine.services.EmailService;
import pet.diploma.sitesearchengine.services.UserService;
import pet.diploma.sitesearchengine.model.response.RegistrationResponse;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

@Controller
public class RegistrationController {

    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public RegistrationController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/api/registration")
    public ResponseEntity<RegistrationResponse> addUser(@RequestBody JwtRequest authRequest) throws MessagingException, UnsupportedEncodingException {
        User newUser = new User();
        newUser.setLogin(authRequest.getLogin());
        newUser.setPassword(authRequest.getPassword());
        newUser.setRoles(Role.USER);
        newUser.setEmailChecked(false);
        System.out.println(newUser);
        RegistrationResponse registrationResponse;
        if (!userService.saveUser(newUser)){
            registrationResponse = new RegistrationResponse(false, "Пользователь с такой почтой уже существует");
            return new ResponseEntity<>(registrationResponse, HttpStatus.BAD_REQUEST);
        }
        emailService.sendCheckCode(authRequest.getLogin());
        return new ResponseEntity<>(new RegistrationResponse(true, null), HttpStatus.OK);
    }

    @GetMapping("/api/verification")
    public ResponseEntity<String> checkCode(@RequestParam String email, @RequestParam int code) {
        boolean checkCode = emailService.getVerification().get(email) == code;
        if (checkCode) {
            userService.updateCheckedUser(email);
        }
        return new ResponseEntity<>(checkCode ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }
}
