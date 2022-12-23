package pet.diploma.sitesearchengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pet.diploma.sitesearchengine.model.Role;
import pet.diploma.sitesearchengine.model.User;
import pet.diploma.sitesearchengine.security.JwtRequest;
import pet.diploma.sitesearchengine.services.UserService;
import pet.diploma.sitesearchengine.model.response.RegistrationResponse;

@Controller
public class RegistrationController {

    private final UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/registration")
    public ResponseEntity<RegistrationResponse> addUser(@RequestBody JwtRequest authRequest) {
        User newUser = new User();
        newUser.setLogin(authRequest.getLogin());
        newUser.setPassword(authRequest.getPassword());
        newUser.setRoles(Role.USER);
        System.out.println(newUser);
        RegistrationResponse registrationResponse;
        if (!userService.saveUser(newUser)){
            registrationResponse = new RegistrationResponse(false, "Пользователь с такой почтой уже существует");
            return new ResponseEntity<>(registrationResponse, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new RegistrationResponse(true, null), HttpStatus.OK);
    }
}
