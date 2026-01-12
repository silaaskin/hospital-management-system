package com.hospital.management.service;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Prescription;
import com.hospital.management.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // LocalDate yerine LocalDateTime eklendi
import java.util.List;
import java.util.Optional;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    public Optional<Prescription> getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id);
    }

    public Optional<Prescription> getPrescriptionByAppointment(Long appointmentId) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı! ID: " + appointmentId));
        return prescriptionRepository.findByAppointment(appointment);
    }

    public List<Prescription> getPrescriptionsByDoctor(Long doctorId) {
        return prescriptionRepository.findByDoctorIdOrderByIdDesc(doctorId);
    }

    public List<Prescription> getPrescriptionsByPatient(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    public Prescription createPrescription(Long appointmentId, Long doctorId, Prescription prescription) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı! ID: " + appointmentId));

        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + doctorId));

        Optional<Prescription> existing = prescriptionRepository.findByAppointment(appointment);
        if (existing.isPresent()) {
            throw new RuntimeException("Bu randevu için zaten bir reçete mevcut!");
        }

        prescription.setAppointment(appointment);
        prescription.setPatient(appointment.getPatient());
        prescription.setDoctor(doctor);

        prescription.setCreatedDate(LocalDateTime.now());

        return prescriptionRepository.save(prescription);
    }

    public Prescription savePrescription(Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }

    public Prescription updatePrescription(Long id, Prescription prescriptionDetails) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reçete bulunamadı! ID: " + id));

        prescription.setPrescriptionText(prescriptionDetails.getPrescriptionText());
        prescription.setDosage(prescriptionDetails.getDosage());
        prescription.setInstructions(prescriptionDetails.getInstructions());
        prescription.setDurationDays(prescriptionDetails.getDurationDays());
        prescription.setNotes(prescriptionDetails.getNotes());

        return prescriptionRepository.save(prescription);
    }

    public void deletePrescription(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reçete bulunamadı! ID: " + id));
        prescriptionRepository.delete(prescription);
    }

    public List<Prescription> getPrescriptionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return prescriptionRepository.findByCreatedDateBetween(startDate, endDate);
    }

    public List<Prescription> getDoctorPrescriptionsByDateRange(Long doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + doctorId));
        return prescriptionRepository.findByDoctorAndDateRange(doctor, startDate, endDate);
    }

    public List<Prescription> getRecentPrescriptionsByPatient(Long patientId) {
        List<Prescription> prescriptions = prescriptionRepository.findRecentByPatientId(patientId);
        return prescriptions.size() > 10 ? prescriptions.subList(0, 10) : prescriptions;
    }
}