package lk.clinic.service.billing;

import java.math.BigDecimal;

public interface BillingStrategy {
    BigDecimal calculateTotal(BigDecimal treatmentFee, BigDecimal consultationFee, BigDecimal discount);
}