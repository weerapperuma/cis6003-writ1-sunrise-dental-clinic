package lk.clinic.service.model;

public record Patient (
   int patientId,
   String patientName,
   String address,
   String contactNumber
){
    // convenience constructor for creating a NEW patient (id not known yet)
    public Patient (
            String patientName,
            String address,
            String contactNumber
    ) {
        this(0,patientName, address, contactNumber);
    }
}
