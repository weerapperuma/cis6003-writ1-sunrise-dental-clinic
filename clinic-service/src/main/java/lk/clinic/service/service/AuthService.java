package lk.clinic.service.service;

import lk.clinic.service.dto.LoginRequest;
import lk.clinic.service.dto.LoginResponse;
import lk.clinic.service.model.User;
import lk.clinic.service.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) { this.userRepository = userRepository; }

    public LoginResponse login(LoginRequest req) {
        System.out.println(">>> [DEBUG] Login attempt: " + req);

        if (req.username() == null || req.username().isBlank() ||
                req.password() == null || req.password().isBlank()) {
            System.out.println(">>> [DEBUG] FAIL: empty username/password");
            return new LoginResponse(false, "Username and password are required", null, null, null);
        }

        User user = userRepository.findByUsername(req.username());
        System.out.println(">>> [DEBUG] User from DB: " + (user == null ? "NULL (not found or inactive)" : user.toString()));

        if (user == null) {
            return new LoginResponse(false, "Invalid username or password", null, null, null);
        }

        boolean pwMatch = BCrypt.checkpw(req.password(), user.passwordHash());
        System.out.println(">>> [DEBUG] BCrypt password match: " + pwMatch);

        if (!pwMatch) {
            return new LoginResponse(false, "Invalid username or password", null, null, null);
        }
        return new LoginResponse(true, "Login successful", user.username(), user.role(), user.fullName());
    }
}
