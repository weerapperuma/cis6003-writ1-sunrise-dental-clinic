package lk.clinic.service.repository;

import lk.clinic.service.model.Bill;
import lk.clinic.service.model.BillItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class BillRepository {

    private final JdbcTemplate jdbc;

    public BillRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean existsByAppointment(int appointmentId) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bills WHERE appointment_id = ?", Integer.class, appointmentId);
        return c != null && c > 0;
    }

    public boolean exists(int billId) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bills WHERE bill_id = ?", Integer.class, billId);
        return c != null && c > 0;
    }

    public int save(Bill bill) {   // total_amount placeholder = 0; the TRIGGER computes it
        KeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO bills (appointment_id, treatment_fee, consultation_fee, discount, " +
                            "total_amount, generated_by) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, bill.appointmentId());
            ps.setBigDecimal(2, bill.treatmentFee());
            ps.setBigDecimal(3, bill.consultationFee());
            ps.setBigDecimal(4, bill.discount());
            ps.setBigDecimal(5, bill.totalAmount());
            ps.setInt(6, bill.generatedBy());
            return ps;
        }, holder);
        return holder.getKey().intValue();
    }

    public Bill findByAppointment(int appointmentId) {
        List<Bill> list = jdbc.query(
                "SELECT bill_id, appointment_id, treatment_fee, consultation_fee, discount, " +
                        "total_amount, payment_status, generated_by FROM bills WHERE appointment_id = ?",
                (rs, i) -> new Bill(rs.getInt("bill_id"), rs.getInt("appointment_id"),
                        rs.getBigDecimal("treatment_fee"), rs.getBigDecimal("consultation_fee"),
                        rs.getBigDecimal("discount"), rs.getBigDecimal("total_amount"),
                        rs.getString("payment_status"), rs.getInt("generated_by")),
                appointmentId);
        return list.isEmpty() ? null : list.get(0);
    }

    public Bill findByBillId(int billId) {
        List<Bill> list = jdbc.query(
                "SELECT bill_id, appointment_id, treatment_fee, consultation_fee, discount, " +
                        "total_amount, payment_status, generated_by FROM bills WHERE bill_id = ?",
                (rs, i) -> new Bill(rs.getInt("bill_id"), rs.getInt("appointment_id"),
                        rs.getBigDecimal("treatment_fee"), rs.getBigDecimal("consultation_fee"),
                        rs.getBigDecimal("discount"), rs.getBigDecimal("total_amount"),
                        rs.getString("payment_status"), rs.getInt("generated_by")),
                billId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<BillItem> findItems(int billId) {
        return jdbc.query(
                "SELECT bill_item_id, bill_id, description, amount FROM bill_items WHERE bill_id = ?",
                (rs, i) -> new BillItem(rs.getInt("bill_item_id"), rs.getInt("bill_id"),
                        rs.getString("description"), rs.getBigDecimal("amount")),
                billId);
    }

    public void saveItem(BillItem item) {
        jdbc.update("INSERT INTO bill_items (bill_id, description, amount) VALUES (?,?,?)",
                item.billId(), item.description(), item.amount());
    }

    public void markPaid(int billId) {
        jdbc.update("UPDATE bills SET payment_status = 'PAID' WHERE bill_id = ?", billId);
    }
}