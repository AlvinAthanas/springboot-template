package tz.co.miugro.Controllers;//package tz.co.miugro.Controllers;
//
//import tz.co.miugro.Model.Entities.User;
//import tz.co.miugro.Repositories.RefreshTokenRepository;
//import tz.co.miugro.Repositories.UserRepository;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class LogoutController {
//    UserRepository userRepository;
//    RefreshTokenRepository refreshTokenRepository;
//
//    public LogoutController(
//            UserRepository userRepository,
//            RefreshTokenRepository refreshTokenRepository) {
//        this.userRepository = userRepository;
//        this.refreshTokenRepository = refreshTokenRepository;
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<?> logout(Authentication auth) {
//        User user = userRepository.findByEmail(auth.getName())
//                .orElseThrow();
//
//        refreshTokenRepository.deleteAllByUserId(user.getId());
//        return ResponseEntity.ok().build();
//    }
//
//}
