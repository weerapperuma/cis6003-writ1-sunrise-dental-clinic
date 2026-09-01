package lk.clinic.service.repository;

import lk.clinic.service.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public User findByUsername(String username) {
        System.out.println(">>> [DEBUG] SQL lookup for username='" + username + "'");
        List<User> users = jdbc.query(
                "SELECT user_id, username, password_hash, full_name, role, active " +
                        "FROM users WHERE username = ? AND active = TRUE",
                (rs, i) -> new User(
                        rs.getInt("user_id"), rs.getString("username"),
                        rs.getString("password_hash"), rs.getString("full_name"),
                        rs.getString("role"), rs.getBoolean("active")),
                username);
        System.out.println(">>> [DEBUG] Rows returned: " + users.size());
        return users.isEmpty() ? null : users.get(0);
    }
}
