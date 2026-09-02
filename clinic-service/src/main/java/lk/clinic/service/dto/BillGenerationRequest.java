package lk.clinic.service.dto;

import java.math.BigDecimal;

public record BillGenerationRequest(
        int appointmentId,
        BigDecimal discount
) {}