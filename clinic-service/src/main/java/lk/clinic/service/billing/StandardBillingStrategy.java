package lk.clinic.service.billing;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class StandardBillingStrategy implements BillingStrategy {
    @Override
    public BigDecimal calculateTotal(BigDecimal treatmentFee, BigDecimal consultationFee, BigDecimal discount) {
        return treatmentFee.add(consultationFee).subtract(discount);
    }
}