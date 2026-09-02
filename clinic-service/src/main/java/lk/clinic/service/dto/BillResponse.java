package lk.clinic.service.dto;

import lk.clinic.service.model.Bill;
import lk.clinic.service.model.BillItem;
import java.util.List;

public record BillResponse(
        boolean success,
        String code,
        String message,
        Bill bill,
        List<BillItem> items
) {
    public static BillResponse ok(Bill bill, List<BillItem> items) {
        return new BillResponse(true, "OK", "Bill generated successfully", bill, items);
    }
    public static BillResponse error(String msg) {
        return new BillResponse(false, "VALIDATION", msg, null, List.of());
    }
    public static BillResponse conflict(String msg) {
        return new BillResponse(false, "CONFLICT", msg, null, List.of());
    }
}