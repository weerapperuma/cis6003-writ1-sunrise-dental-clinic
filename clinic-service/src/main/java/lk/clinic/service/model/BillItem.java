package lk.clinic.service.model;

import java.math.BigDecimal;

public record BillItem(
        int billItemId,
        int billId,
        String description,
        BigDecimal amount
) {}