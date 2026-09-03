package lk.clinic.service.service;

import lk.clinic.service.dto.LoginRequest;
import lk.clinic.service.dto.LoginResponse;
import lk.clinic.service.model.User;
import lk.clinic.service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository);
    }

    @Test
    @DisplayName("Should fail when username or password is blank")
    void testBlankCredentials() {
        LoginResponse r1 = authService.login(new LoginRequest("", "password"));
        assertFalse(r1.success());
        assertEquals("Username and password are required", r1.message());

        LoginResponse r2 = authService.login(new LoginRequest("admin", "  "));
        assertFalse(r2.success());
        assertEquals("Username and password are required", r2.message());

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should fail when user does not exist in repository")
    void testUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(null);

        LoginResponse res = authService.login(new LoginRequest("unknown", "secret"));

        assertFalse(res.success());
        assertEquals("Invalid username or password", res.message());
        verify(userRepository, times(1)).findByUsername("unknown");
    }

    @Test
    @DisplayName("Should login successfully when BCrypt password matches")
    void testSuccessfulLoginBCrypt() {
        String rawPassword = "password123";
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        User mockUser = new User(1, "admin", hashed, "System Administrator", "ADMIN", true);

        when(userRepository.findByUsername("admin")).thenReturn(mockUser);

        LoginResponse res = authService.login(new LoginRequest("admin", rawPassword));

        assertTrue(res.success());
        assertEquals("Login successful", res.message());
        assertEquals("admin", res.username());
        assertEquals("ADMIN", res.role());
        assertEquals("System Administrator", res.fullName());
    }

    @Test
    @DisplayName("Should reject login when BCrypt password is wrong")
    void testWrongPassword() {
        String hashed = BCrypt.hashpw("correctPassword", BCrypt.gensalt());
        User mockUser = new User(2, "reception", hashed, "Front Desk", "RECEPTIONIST", true);

        when(userRepository.findByUsername("reception")).thenReturn(mockUser);

        LoginResponse res = authService.login(new LoginRequest("reception", "wrongPassword"));

        assertFalse(res.success());
        assertEquals("Invalid username or password", res.message());
    }
}
