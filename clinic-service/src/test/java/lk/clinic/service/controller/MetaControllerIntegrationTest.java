package lk.clinic.service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MetaControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        MetaController controller = new MetaController(jdbc);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/meta/dentists - Should return list of active dentists")
    void testGetDentists() throws Exception {
        List<Map<String, Object>> mockDentists = List.of(
                Map.of("id", 1, "name", "Dr. Sunila Jayawardena"),
                Map.of("id", 2, "name", "Dr. Samantha Perera")
        );

        when(jdbc.queryForList(anyString())).thenReturn(mockDentists);

        mockMvc.perform(get("/api/meta/dentists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Dr. Sunila Jayawardena"));
    }

    @Test
    @DisplayName("GET /api/meta/treatments - Should return list of treatments with fee structures")
    void testGetTreatments() throws Exception {
        List<Map<String, Object>> mockTreatments = List.of(
                Map.of("id", 1, "name", "Cleaning", "fee", new BigDecimal("3000.00"), "consultFee", new BigDecimal("1000.00")),
                Map.of("id", 2, "name", "Extraction", "fee", new BigDecimal("4500.00"), "consultFee", new BigDecimal("1500.00"))
        );

        when(jdbc.queryForList(anyString())).thenReturn(mockTreatments);

        mockMvc.perform(get("/api/meta/treatments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Cleaning"))
                .andExpect(jsonPath("$[0].fee").value(3000.00))
                .andExpect(jsonPath("$[0].consultFee").value(1000.00));
    }
}
