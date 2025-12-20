package com.hospital.management.service;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Prescription;
import com.hospital.management.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    // Tüm reçeteleri listele
    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    // ID'ye göre reçete bul
    public Optional<Prescription> getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id);
    }

    // Randevuya göre reçete bul
    public Optional<Prescription> getPrescriptionByAppointment(Long appointmentId) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı! ID: " + appointmentId));
        return prescriptionRepository.findByAppointment(appointment);
    }

    // Doktora göre reçeteleri listele
    public List<Prescription> getPrescriptionsByDoctor(Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + doctorId));
        return prescriptionRepository.findByDoctor(doctor);
    }

    // Hastaya göre reçeteleri listele
    public List<Prescription> getPrescriptionsByPatient(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    // Yeni reçete oluştur
    public Prescription createPrescription(Long appointmentId, Long doctorId, Prescription prescription) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı! ID: " + appointmentId));

        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + doctorId));

        // Aynı randevu için reçete var mı kontrol et
        Optional<Prescription> existing = prescriptionRepository.findByAppointment(appointment);
        if (existing.isPresent()) {
            throw new RuntimeException("Bu randevu için zaten bir reçete mevcut!");
        }

        prescription.setAppointment(appointment);
        prescription.setDoctor(doctor);
        prescription.setPrescriptionDate(LocalDate.now());

        return prescriptionRepository.save(prescription);
    }

    // Reçete kaydet/güncelle
    public Prescription savePrescription(Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }

    // Reçete güncelle
    public Prescription updatePrescription(Long id, Prescription prescriptionDetails) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reçete bulunamadı! ID: " + id));

        prescription.setMedications(prescriptionDetails.getMedications());
        prescription.setDosage(prescriptionDetails.getDosage());
        prescription.setInstructions(prescriptionDetails.getInstructions());
        prescription.setDurationDays(prescriptionDetails.getDurationDays());
        prescription.setNotes(prescriptionDetails.getNotes());

        return prescriptionRepository.save(prescription);
    }

    // Reçete sil
    public void deletePrescription(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reçete bulunamadı! ID: " + id));
        prescriptionRepository.delete(prescription);
    }

    // Tarih aralığına göre reçeteleri getir
    public List<Prescription> getPrescriptionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return prescriptionRepository.findByPrescriptionDateBetween(startDate, endDate);
    }

    // Doktorun belirli tarihteki reçetelerini getir
    public List<Prescription> getDoctorPrescriptionsByDateRange(Long doctorId, LocalDate startDate, LocalDate endDate) {
        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + doctorId));
        return prescriptionRepository.findByDoctorAndDateRange(doctor, startDate, endDate);
    }

    // Hastanın son reçetelerini getir (en fazla 10 adet)
    public List<Prescription> getRecentPrescriptionsByPatient(Long patientId) {
        List<Prescription> prescriptions = prescriptionRepository.findRecentByPatientId(patientId);
        return prescriptions.size() > 10 ? prescriptions.subList(0, 10) : prescriptions;
    }
}