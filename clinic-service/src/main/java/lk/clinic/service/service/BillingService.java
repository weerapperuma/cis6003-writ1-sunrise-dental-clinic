package lk.clinic.service.service;

import lk.clinic.service.billing.BillingStrategy;
import lk.clinic.service.dto.AppointmentBillingInfo;
import lk.clinic.service.dto.BillGenerationRequest;
import lk.clinic.service.dto.BillResponse;
import lk.clinic.service.model.Bill;
import lk.clinic.service.model.BillItem;
import lk.clinic.service.repository.AppointmentRepository;
import lk.clinic.service.repository.BillRepository;
import lk.clinic.service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BillingService {

    private final BillRepository billRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillingStrategy billingStrategy;
    private final UserRepository userRepository;

    public BillingService(BillRepository billRepository, AppointmentRepository appointmentRepository,
                          BillingStrategy billingStrategy, UserRepository userRepository) {
        this.billRepository = billRepository;
        this.appointmentRepository = appointmentRepository;
        this.billingStrategy = billingStrategy;
        this.userRepository = userRepository;
    }

    @Transactional
    public BillResponse generate(BillGenerationRequest req, String username) {

        BigDecimal discount = req.discount() == null ? BigDecimal.ZERO : req.discount();
        if (discount.signum() < 0) {
            return BillResponse.error("Discount cannot be negative.");
        }

        AppointmentBillingInfo info = appointmentRepository.findBillingInfo(req.appointmentId());
        if (info == null) {
            return BillResponse.error("Appointment not found.");
        }

        if (billRepository.existsByAppointment(req.appointmentId())) {
            return BillResponse.conflict("A bill already exists for this appointment (1:1 rule).");
        }

        // Guard: discount must not exceed the sum of treatment and consultation fees
        BigDecimal totalFees = info.treatmentFee().add(info.consultationFee());
        if (discount.compareTo(totalFees) > 0) {
            return BillResponse.error(
                    "Discount (" + discount + ") cannot exceed total fees (" + totalFees + ").");
        }

        // Strategy preview (cross-checked against the DB trigger result below)
        BigDecimal preview = billingStrategy.calculateTotal(
                info.treatmentFee(), info.consultationFee(), discount);

        int userId = userRepository.findByUsername(username).userId();

        Bill bill = new Bill(0, req.appointmentId(), info.treatmentFee(),
                info.consultationFee(), discount, BigDecimal.ZERO, "PENDING", userId);
        int billId = billRepository.save(bill);          // trigger computes total_amount

        billRepository.saveItem(new BillItem(0, billId, "Consultation Fee", info.consultationFee()));
        billRepository.saveItem(new BillItem(0, billId, "Treatment Fee", info.treatmentFee()));
        if (discount.signum() > 0) {
            billRepository.saveItem(new BillItem(0, billId, "Discount", discount.negate()));
        }

        Bill saved = billRepository.findByAppointment(req.appointmentId());
        if (preview.compareTo(saved.totalAmount()) != 0) {
            System.out.println(">>> [WARN] Strategy preview " + preview +
                    " != trigger total " + saved.totalAmount());
        }

        return BillResponse.ok(saved, billRepository.findItems(billId));
    }

    public BillResponse details(int appointmentId) {
        Bill bill = billRepository.findByAppointment(appointmentId);
        if (bill == null) return null;
        return BillResponse.ok(bill, billRepository.findItems(bill.billId()));
    }

    public BillResponse detailsByBillId(int billId) {
        Bill bill = billRepository.findByBillId(billId);
        if (bill == null) return null;
        return BillResponse.ok(bill, billRepository.findItems(bill.billId()));
    }

    public boolean markPaid(int billId) {
        if (!billRepository.exists(billId)) return false;
        billRepository.markPaid(billId);
        return true;
    }
}