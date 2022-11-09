package pet.skillbox.sitesearchengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pet.skillbox.sitesearchengine.model.response.AdminResponse;
import pet.skillbox.sitesearchengine.services.UserService;

@   Controller
public class AdminController {
    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/api/admin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AdminResponse> userList() {
        AdminResponse adminResponse = new AdminResponse(userService.getAllUsers());
        return new ResponseEntity<>(adminResponse, HttpStatus.OK);
    }

    @PostMapping(path = "/api/admin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AdminResponse> deleteUser(@RequestParam(defaultValue = "" ) Long userId,
                              @RequestParam(defaultValue = "" ) String action) {
        if (action.equals("delete")){
            userService.deleteUser(userId);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
