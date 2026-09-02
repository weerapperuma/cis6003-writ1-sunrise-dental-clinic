package lk.clinic.service.model;

import java.math.BigDecimal;

public record Bill(
        int billId,
        int appointmentId,
        BigDecimal treatmentFee,
        BigDecimal consultationFee,
        BigDecimal discount,
        BigDecimal totalAmount,
        String paymentStatus,
        int generatedBy
) {}