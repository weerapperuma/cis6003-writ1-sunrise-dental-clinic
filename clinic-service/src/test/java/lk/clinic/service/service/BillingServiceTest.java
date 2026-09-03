package lk.clinic.service.service;

import lk.clinic.service.billing.BillingStrategy;
import lk.clinic.service.dto.AppointmentBillingInfo;
import lk.clinic.service.dto.BillGenerationRequest;
import lk.clinic.service.dto.BillResponse;
import lk.clinic.service.model.Bill;
import lk.clinic.service.model.BillItem;
import lk.clinic.service.model.User;
import lk.clinic.service.repository.AppointmentRepository;
import lk.clinic.service.repository.BillRepository;
import lk.clinic.service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BillRepository billRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private BillingStrategy billingStrategy;
    @Mock
    private UserRepository userRepository;

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingService(
                billRepository,
                appointmentRepository,
                billingStrategy,
                userRepository
        );
    }

    @Test
    @DisplayName("Should generate bill successfully with calculated items and total")
    void testSuccessfulBillGeneration() {
        int apptId = 5;
        BigDecimal treatmentFee = new BigDecimal("4000.00");
        BigDecimal consultFee = new BigDecimal("1500.00");
        BigDecimal discount = new BigDecimal("500.00");
        BigDecimal expectedTotal = new BigDecimal("5000.00");

        AppointmentBillingInfo info = new AppointmentBillingInfo(apptId, "APT-20260902-0001", treatmentFee, consultFee);
        Bill savedBill = new Bill(101, apptId, treatmentFee, consultFee, discount, expectedTotal, "PENDING", 1);
        List<BillItem> items = List.of(
                new BillItem(1, 101, "Consultation Fee", consultFee),
                new BillItem(2, 101, "Treatment Fee", treatmentFee),
                new BillItem(3, 101, "Discount", discount.negate())
        );

        when(appointmentRepository.findBillingInfo(apptId)).thenReturn(info);
        when(billRepository.existsByAppointment(apptId)).thenReturn(false);
        when(billingStrategy.calculateTotal(treatmentFee, consultFee, discount)).thenReturn(expectedTotal);
        when(userRepository.findByUsername("admin")).thenReturn(new User(1, "admin", "hash", "Admin", "ADMIN", true));
        when(billRepository.save(any(Bill.class))).thenReturn(101);
        when(billRepository.findByAppointment(apptId)).thenReturn(savedBill);
        when(billRepository.findItems(101)).thenReturn(items);

        BillGenerationRequest req = new BillGenerationRequest(apptId, discount);
        BillResponse res = billingService.generate(req, "admin");

        assertTrue(res.success());
        assertEquals("OK", res.code());
        assertNotNull(res.bill());
        assertEquals(expectedTotal, res.bill().totalAmount());
        assertEquals(3, res.items().size());
        verify(billRepository, times(3)).saveItem(any(BillItem.class));
    }

    @Test
    @DisplayName("Should reject bill generation when discount is negative")
    void testNegativeDiscountRejection() {
        BillGenerationRequest req = new BillGenerationRequest(5, new BigDecimal("-100.00"));

        BillResponse res = billingService.generate(req, "admin");

        assertFalse(res.success());
        assertEquals("Discount cannot be negative.", res.message());
        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(billRepository);
    }

    @Test
    @DisplayName("Should return error when appointment is not found")
    void testAppointmentNotFound() {
        when(appointmentRepository.findBillingInfo(999)).thenReturn(null);

        BillGenerationRequest req = new BillGenerationRequest(999, BigDecimal.ZERO);
        BillResponse res = billingService.generate(req, "admin");

        assertFalse(res.success());
        assertEquals("Appointment not found.", res.message());
        verify(billRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should enforce 1:1 rule and return CONFLICT when bill already exists for appointment")
    void testDuplicateBillConflictEnforcing1to1Rule() {
        int apptId = 5;
        AppointmentBillingInfo info = new AppointmentBillingInfo(apptId, "APT-20260902-0001", new BigDecimal("3000.00"), new BigDecimal("1200.00"));
        when(appointmentRepository.findBillingInfo(apptId)).thenReturn(info);
        when(billRepository.existsByAppointment(apptId)).thenReturn(true);

        BillGenerationRequest req = new BillGenerationRequest(apptId, BigDecimal.ZERO);
        BillResponse res = billingService.generate(req, "admin");

        assertFalse(res.success());
        assertEquals("CONFLICT", res.code());
        assertTrue(res.message().contains("1:1 rule"));
        verify(billRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve bill details by appointment ID")
    void testDetailsByAppointment() {
        Bill mockBill = new Bill(10, 5, new BigDecimal("3000.00"), new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("4000.00"), "PAID", 1);
        when(billRepository.findByAppointment(5)).thenReturn(mockBill);
        when(billRepository.findItems(10)).thenReturn(List.of());

        BillResponse res = billingService.details(5);

        assertNotNull(res);
        assertTrue(res.success());
        assertEquals(10, res.bill().billId());

        when(billRepository.findByAppointment(999)).thenReturn(null);
        assertNull(billingService.details(999));
    }

    @Test
    @DisplayName("Should mark bill as PAID when bill exists")
    void testMarkPaid() {
        when(billRepository.exists(10)).thenReturn(true);
        boolean ok = billingService.markPaid(10);
        assertTrue(ok);
        verify(billRepository, times(1)).markPaid(10);

        when(billRepository.exists(999)).thenReturn(false);
        boolean notFound = billingService.markPaid(999);
        assertFalse(notFound);
        verify(billRepository, never()).markPaid(999);
    }
}
