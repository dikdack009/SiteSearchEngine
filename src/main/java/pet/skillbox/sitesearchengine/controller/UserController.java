//package pet.skillbox.sitesearchengine.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//import pet.skillbox.sitesearchengine.model.User;
//import pet.skillbox.sitesearchengine.model.response.AdminResponse;
//import pet.skillbox.sitesearchengine.model.response.RegistrationResponse;
//import pet.skillbox.sitesearchengine.services.UserService;
//
//import java.util.List;
//
//@RestController
//public class UserController {
//
//    private UserService service;
//
//    @Autowired
//    public UserController(UserService service) {
//        this.service = service;
//    }
//
//    @GetMapping(path = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<AdminResponse> getAll() {
//        return new ResponseEntity<>(new AdminResponse(service.getAllUsers()), HttpStatus.OK);
//    }
//}