package lk.clinic.service.model;

public record User(
        int userId,
        String username,
        String passwordHash,
        String fullName,
        String role,
        boolean active) {
}
