package co.tz.sheriaconnectapi.Services.UserServices;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import co.tz.sheriaconnectapi.Abstractions.Command;
import co.tz.sheriaconnectapi.Exceptions.ErrorMessages;
import co.tz.sheriaconnectapi.Exceptions.UserNotValidException;
import co.tz.sheriaconnectapi.Model.DTOs.UserDTO;
import co.tz.sheriaconnectapi.Model.Entities.User;
import co.tz.sheriaconnectapi.Repositories.UserRepository;


@Service
public class CreateUserService implements Command<User, UserDTO> {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUserService(UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseEntity<UserDTO> execute(User user) {
        return execute(user, null);
    }

    public ResponseEntity<UserDTO> execute(User user, HttpServletRequest request) {
        if (!userRepository.existsByEmail(user.getEmail())) {
            System.out.println();
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            throw new UserNotValidException(ErrorMessages.EMAIL_ALREADY_EXISTS.getMessage());
        }




        // First, save the user to generate an ID
        user = userRepository.save(user);
        // Finally, save the user again to update the role ID
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserDTO(user));
    }
}
