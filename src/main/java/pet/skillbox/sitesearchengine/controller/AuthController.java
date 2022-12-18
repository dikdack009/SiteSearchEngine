package pet.skillbox.sitesearchengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pet.skillbox.sitesearchengine.model.User;
import pet.skillbox.sitesearchengine.services.UserService;

import java.util.Objects;

@RestController
public class AuthController {

    private final UserService service;

    @Autowired
    public AuthController(UserService service) {
        this.service = service;
    }

    @PostMapping(path = "/api/auth/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody UserDetails getAuthUser() {
        //
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(auth);
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        User user = (principal instanceof User) ? (User) principal : null;
        assert user != null;
        System.out.println(user);
        return Objects.nonNull(user) ? service.checkUser(user) : null;
    }
}
