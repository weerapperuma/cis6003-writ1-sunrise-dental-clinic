package lk.clinic.service.billing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StandardBillingStrategyTest {

    private StandardBillingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new StandardBillingStrategy();
    }

    @Test
    @DisplayName("Should compute total as (treatmentFee + consultationFee) - discount correctly")
    void testStandardCalculationWithDiscount() {
        BigDecimal treatmentFee = new BigDecimal("4500.00");
        BigDecimal consultFee = new BigDecimal("1500.00");
        BigDecimal discount = new BigDecimal("500.00");

        BigDecimal total = strategy.calculateTotal(treatmentFee, consultFee, discount);

        assertEquals(new BigDecimal("5500.00"), total);
    }

    @Test
    @DisplayName("Should compute total correctly when discount is zero")
    void testStandardCalculationZeroDiscount() {
        BigDecimal treatmentFee = new BigDecimal("3000.00");
        BigDecimal consultFee = new BigDecimal("1200.00");
        BigDecimal discount = BigDecimal.ZERO;

        BigDecimal total = strategy.calculateTotal(treatmentFee, consultFee, discount);

        assertEquals(new BigDecimal("4200.00"), total);
    }

    @Test
    @DisplayName("Should handle 100% discount correctly resulting in zero total")
    void testFullDiscount() {
        BigDecimal treatmentFee = new BigDecimal("2000.00");
        BigDecimal consultFee = new BigDecimal("1000.00");
        BigDecimal discount = new BigDecimal("3000.00");

        BigDecimal total = strategy.calculateTotal(treatmentFee, consultFee, discount);

        assertEquals(new BigDecimal("0.00"), total);
    }
}
