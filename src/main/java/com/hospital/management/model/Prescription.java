package com.hospital.management.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // Alan ismi 'prescriptionDate' yerine 'createdDate' yapıldı
    @Column(nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    // Alan ismi 'medications' yerine 'prescriptionText' yapıldı
    @Column(nullable = false, columnDefinition = "TEXT")
    private String prescriptionText;

    @Column(columnDefinition = "TEXT")
    private String dosage;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    private Integer durationDays;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Prescription() {}

    // Getter ve Setterlar
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public String getPrescriptionText() { return prescriptionText; }
    public void setPrescriptionText(String prescriptionText) { this.prescriptionText = prescriptionText; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}