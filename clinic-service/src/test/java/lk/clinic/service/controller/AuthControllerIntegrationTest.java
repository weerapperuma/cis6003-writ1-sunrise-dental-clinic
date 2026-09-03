package lk.clinic.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.clinic.service.dto.LoginRequest;
import lk.clinic.service.dto.LoginResponse;
import lk.clinic.service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("POST /api/auth/login - Should succeed and set session when credentials are valid")
    void testLoginSuccess() throws Exception {
        LoginRequest req = new LoginRequest("admin", "password123");
        LoginResponse mockRes = new LoginResponse(true, "Login successful", "admin", "ADMIN", "System Administrator");

        when(authService.login(any(LoginRequest.class))).thenReturn(mockRes);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.fullName").value("System Administrator"))
                .andExpect(request().sessionAttribute("loggedInUser", "admin"))
                .andExpect(request().sessionAttribute("role", "ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 Unauthorized when credentials are invalid")
    void testLoginFailure() throws Exception {
        LoginRequest req = new LoginRequest("admin", "wrong");
        LoginResponse mockRes = new LoginResponse(false, "Invalid username or password", null, null, null);

        when(authService.login(any(LoginRequest.class))).thenReturn(mockRes);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("GET /api/auth/me - Should return 401 when no session exists")
    void testMeWithoutSession() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/auth/me - Should return 200 with identity when session is active")
    void testMeWithActiveSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "reception");
        session.setAttribute("role", "RECEPTIONIST");

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("reception"))
                .andExpect(jsonPath("$.role").value("RECEPTIONIST"));
    }

    @Test
    @DisplayName("POST /api/auth/logout - Should invalidate session and return 200")
    void testLogout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "admin");

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));
    }
}
