package lk.clinic.service.controller;

import jakarta.servlet.http.HttpSession;
import lk.clinic.service.dto.LoginRequest;
import lk.clinic.service.dto.LoginResponse;
import lk.clinic.service.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/auth", "api/auth"})
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        System.out.println(">>> [DEBUG] POST /api/auth/login RECEIVED: " + request);
        LoginResponse res = authService.login(request);
        System.out.println(">>> [DEBUG] Response: success=" + res.success() + ", msg=" + res.message());
        if (res.success()) {
            session.setAttribute("loggedInUser", res.username());
            session.setAttribute("role", res.role());
        }
        return res.success() ? ResponseEntity.ok(res) : ResponseEntity.status(401).body(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return ResponseEntity.status(401).body(Map.of("success", false));
        return ResponseEntity.ok(Map.of("success", true, "username", username,
                "role", session.getAttribute("role")));
    }
}
