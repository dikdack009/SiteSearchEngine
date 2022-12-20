package pet.skillbox.sitesearchengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pet.skillbox.sitesearchengine.model.Role;
import pet.skillbox.sitesearchengine.model.User;
import pet.skillbox.sitesearchengine.model.response.RegistrationResponse;
import pet.skillbox.sitesearchengine.services.UserService;

@Controller
public class RegistrationController {

    private final UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/registration")
    public ResponseEntity<RegistrationResponse> addUser(@RequestParam(name = "email")String userName,
                                                        @RequestParam(name = "password")String password,
                                                        @RequestParam(name = "cpassword")String confirmPassword) {
        User newUser = new User();
        newUser.setLogin(userName);
        newUser.setPassword(password);
        newUser.setRoles(Role.USER);
        System.out.println(newUser);
        RegistrationResponse registrationResponse;
        if (!password.equals(confirmPassword)){
            registrationResponse = new RegistrationResponse(false, "Пароли не совпадают");
            return new ResponseEntity<>(registrationResponse, HttpStatus.BAD_REQUEST);
        }
        if (!userService.saveUser(newUser)){
            registrationResponse = new RegistrationResponse(false, "Пользователь с такой почтой уже существует");
            return new ResponseEntity<>(registrationResponse, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new RegistrationResponse(true, null), HttpStatus.OK);
    }
}
