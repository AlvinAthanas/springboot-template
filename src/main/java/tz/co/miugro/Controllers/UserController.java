package co.tz.sheriaconnectapi.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import co.tz.sheriaconnectapi.Model.DTOs.UserDTO;
import co.tz.sheriaconnectapi.Model.Entities.User;
import co.tz.sheriaconnectapi.Services.UserServices.CreateUserService;

@RestController
public class UserController {
    private final CreateUserService createUserService;

    public UserController(CreateUserService createUserService) {
        this.createUserService = createUserService;
    }

    @PostMapping("/user")
    public ResponseEntity<UserDTO> createUser(@RequestBody User user, HttpServletRequest request){
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            // Password was not provided → set a default password
            String defaultPassword = user.getEmail();
            user.setPassword(defaultPassword);
        }
        return  createUserService.execute(user, request);
    }
}
