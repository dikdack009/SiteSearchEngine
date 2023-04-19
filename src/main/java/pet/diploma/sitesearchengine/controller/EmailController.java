package pet.diploma.sitesearchengine.controller;

import org.apache.logging.log4j.LogManager;
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
        LogManager.getLogger("index").info(re.getLogin() + ":\tОтправляем письмо восстановления на почту");
        if (checkFailedEmailFormat(re.getLogin())) {
            LogManager.getLogger("index").error(re.getLogin() + ":\tОшибка отправки письма: Неверный формат почты");
            return new ResponseEntity<>(new RegistrationResponse(false, "Неверный формат почты"), HttpStatus.BAD_REQUEST);
        }
        Optional<User> optionalUser = userService.getByLogin(re.getLogin());
        if (optionalUser.isPresent()) {
            try {
                emailService.sendRecoverCode(re.getLogin());
                LogManager.getLogger("index").info(re.getLogin() + ":\tПисьмо восстановления отправлено");
                return new ResponseEntity<>(new RegistrationResponse(true, null), HttpStatus.OK);
            } catch (MessagingException e) {
                LogManager.getLogger("index").debug(re.getLogin() + ":\tВнутренняя ошибка: " + e.getMessage());
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
