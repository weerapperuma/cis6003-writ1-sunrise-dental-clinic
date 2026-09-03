package lk.clinic.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.clinic.service.dto.BillGenerationRequest;
import lk.clinic.service.dto.BillResponse;
import lk.clinic.service.model.Bill;
import lk.clinic.service.model.BillItem;
import lk.clinic.service.service.BillingService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BillControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private BillingService billingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        BillController controller = new BillController(billingService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("POST /api/bills - Should reject with 401 Unauthorized when unauthenticated")
    void testGenerateUnauthenticated() throws Exception {
        BillGenerationRequest req = new BillGenerationRequest(5, BigDecimal.ZERO);

        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/bills - Should return 201 Created on valid bill generation")
    void testGenerateSuccess() throws Exception {
        BillGenerationRequest req = new BillGenerationRequest(5, new BigDecimal("500.00"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "admin");

        Bill mockBill = new Bill(1, 5, new BigDecimal("4000.00"), new BigDecimal("1500.00"), new BigDecimal("500.00"), new BigDecimal("5000.00"), "PENDING", 1);
        List<BillItem> items = List.of(
                new BillItem(1, 1, "Consultation Fee", new BigDecimal("1500.00")),
                new BillItem(2, 1, "Treatment Fee", new BigDecimal("4000.00"))
        );
        BillResponse mockRes = BillResponse.ok(mockBill, items);

        when(billingService.generate(any(BillGenerationRequest.class), eq("admin"))).thenReturn(mockRes);

        mockMvc.perform(post("/api/bills")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.bill.totalAmount").value(5000.00))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/bills - Should return 409 Conflict when bill already exists for appointment")
    void testGenerateDuplicateConflict() throws Exception {
        BillGenerationRequest req = new BillGenerationRequest(5, BigDecimal.ZERO);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "admin");

        when(billingService.generate(any(BillGenerationRequest.class), eq("admin")))
                .thenReturn(BillResponse.conflict("A bill already exists for this appointment (1:1 rule)."));

        mockMvc.perform(post("/api/bills")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @DisplayName("GET /api/bills?appointmentId=5 - Should return 200 with bill details when found")
    void testGetBillDetailsSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "admin");

        Bill mockBill = new Bill(1, 5, new BigDecimal("4000.00"), new BigDecimal("1500.00"), BigDecimal.ZERO, new BigDecimal("5500.00"), "PENDING", 1);
        BillResponse mockRes = BillResponse.ok(mockBill, List.of());

        when(billingService.details(5)).thenReturn(mockRes);

        mockMvc.perform(get("/api/bills?appointmentId=5").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.bill.appointmentId").value(5));
    }

    @Test
    @DisplayName("GET /api/bills?appointmentId=999 - Should return 404 when bill does not exist")
    void testGetBillDetailsNotFound() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "admin");

        when(billingService.details(999)).thenReturn(null);

        mockMvc.perform(get("/api/bills?appointmentId=999").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/bills/1/pay - Should mark bill as PAID and return 200")
    void testMarkPaidSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "admin");

        when(billingService.markPaid(1)).thenReturn(true);

        mockMvc.perform(post("/api/bills/1/pay").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bill marked as PAID."));
    }
}
