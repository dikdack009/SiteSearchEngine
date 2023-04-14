package pet.diploma.sitesearchengine.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pet.diploma.sitesearchengine.model.User;
import pet.diploma.sitesearchengine.model.response.EmailRequest;
import pet.diploma.sitesearchengine.model.response.RegistrationResponse;
import pet.diploma.sitesearchengine.services.EmailService;
import pet.diploma.sitesearchengine.services.UserService;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

@RestController
@RequestMapping("api/email")
public class EmailController {
    private final EmailService emailService;
    private final UserService userService;
    private final static String EMAIL_REGEX = "([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\\.[a-zA-Z0-9_-]+)";

    @Autowired
    public EmailController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/recover")
    public ResponseEntity<RegistrationResponse> sendRecoverEmail(@NotNull @RequestBody EmailRequest re) throws UnsupportedEncodingException {
        System.out.println(re);
        System.out.println(re.getLogin().matches(EMAIL_REGEX));
        if (checkFailedEmailFormat(re.getLogin())) {
            return new ResponseEntity<>(new RegistrationResponse(false, "Неверный формат почты"), HttpStatus.BAD_REQUEST);
        }
        Optional<User> optionalUser = userService.getByLogin(re.getLogin());
        if (optionalUser.isPresent()) {
            try {
                emailService.sendRecoverCode(re.getLogin());
                return new ResponseEntity<>(new RegistrationResponse(true, null), HttpStatus.OK);
            } catch (MessagingException e) {
                return new ResponseEntity<>(new RegistrationResponse(false, "Письмо не отправлено"), HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(new RegistrationResponse(false, "Пользователь не найден"), HttpStatus.NOT_FOUND);
        }
    }

    private boolean checkFailedEmailFormat(String email) {
        return email.isEmpty() || !email.matches(EMAIL_REGEX);
    }
}