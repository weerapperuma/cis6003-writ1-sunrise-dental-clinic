package lk.clinic.service.controller;

import jakarta.servlet.http.HttpSession;
import lk.clinic.service.dto.BillGenerationRequest;
import lk.clinic.service.dto.BillResponse;
import lk.clinic.service.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillingService billingService;

    public BillController(BillingService billingService) { this.billingService = billingService; }

    private String user(HttpSession session) {
        return (String) session.getAttribute("loggedInUser");
    }

    @PostMapping({"", "/"})
    public ResponseEntity<BillResponse> generate(@RequestBody BillGenerationRequest request,
                                                 HttpSession session) {
        if (user(session) == null) {
            return ResponseEntity.status(401).body(
                    new BillResponse(false, "AUTH", "Login required to generate bills.", null, java.util.List.of()));
        }
        BillResponse res = billingService.generate(request, user(session));
        return switch (res.code()) {
            case "OK"       -> ResponseEntity.status(201).body(res);
            case "CONFLICT" -> ResponseEntity.status(409).body(res);
            default         -> ResponseEntity.badRequest().body(res);
        };
    }

    @GetMapping({"", "/"})
    public ResponseEntity<?> getByAppointment(
            @RequestParam(name = "appointmentId", required = false) Integer appointmentId,
            @RequestParam(name = "billId", required = false) Integer billId,
            HttpSession session) {
        if (user(session) == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Login required."));
        }
        if (appointmentId != null) {
            BillResponse res = billingService.details(appointmentId);
            if (res == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "No bill found for this appointment."));
            }
            return ResponseEntity.ok(res);
        }
        if (billId != null) {
            BillResponse res = billingService.detailsByBillId(billId);
            if (res == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "No bill found for bill ID " + billId));
            }
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "appointmentId or billId parameter is required."));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getByAppointmentPath(@PathVariable(name = "appointmentId") int appointmentId, HttpSession session) {
        if (user(session) == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Login required."));
        }
        BillResponse res = billingService.details(appointmentId);
        if (res == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "No bill found for this appointment."));
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{billId}")
    public ResponseEntity<?> getByBillIdPath(@PathVariable(name = "billId") int billId, HttpSession session) {
        if (user(session) == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Login required."));
        }
        BillResponse res = billingService.detailsByBillId(billId);
        if (res == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "No bill found for bill ID " + billId));
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{billId}/pay")
    public ResponseEntity<?> pay(@PathVariable(name = "billId") int billId, HttpSession session) {
        if (user(session) == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Login required."));
        }
        boolean ok = billingService.markPaid(billId);
        if (!ok) return ResponseEntity.status(404).body(Map.of("success", false, "message", "Bill not found."));
        return ResponseEntity.ok(Map.of("success", true, "message", "Bill marked as PAID."));
    }
}