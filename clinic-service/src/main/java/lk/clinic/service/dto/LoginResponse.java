package lk.clinic.service.dto;

public record LoginResponse(
        boolean success,
        String message,
        String username,
        String role,
        String fullName
) {
}
