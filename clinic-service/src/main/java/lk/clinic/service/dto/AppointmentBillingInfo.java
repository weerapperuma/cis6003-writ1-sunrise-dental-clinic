package lk.clinic.service.dto;

import java.math.BigDecimal;

public record AppointmentBillingInfo(
        int appointmentId,
        String appointmentNumber,
        BigDecimal treatmentFee,
        BigDecimal consultationFee
) {}