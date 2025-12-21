package com.hospital.management.service;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Appointment.AppointmentStatus;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    // Hastaya göre randevuları listele
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı! ID: " + patientId));
        return appointmentRepository.findByPatient(patient);
    }

    // Doktora göre randevuları listele
    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + doctorId));
        return appointmentRepository.findByDoctor(doctor);
    }

    // --- HATALARI GİDEREN YENİ METODLAR ---

    // 1. Duruma göre randevuları listele (ONAYLANDI, İPTAL vb.)
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    // 2. Hastanın GELECEK randevularını listele
    public List<Appointment> getUpcomingAppointmentsByPatient(Long patientId) {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı!"));
        return appointmentRepository.findUpcomingAppointmentsByPatient(patient, LocalDateTime.now());
    }

    // 3. Tarih aralığına göre randevular
    public List<Appointment> getAppointmentsByDateRange(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByAppointmentDateBetween(start, end);
    }

    // 4. Randevu Oluşturma (30 Dakika Çakışma Kontrolü Dahil)
    public Appointment createAppointment(Long patientId, Long doctorId, LocalDateTime appointmentDate) {
        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Geçmiş tarih için randevu oluşturamazsınız!");
        }

        Doctor doctor = doctorService.getDoctorById(doctorId).orElseThrow();

        // 30 Dakikalık Aralık Kontrolü
        LocalDateTime startRange = appointmentDate.minusMinutes(29);
        LocalDateTime endRange = appointmentDate.plusMinutes(29);

        // Repository üzerinden o aralıkta doktorun başka bir AKTİF randevusu var mı bakılır
        List<Appointment> conflicts = appointmentRepository.findDoctorAppointmentsByDateAndStatus(
                doctor, startRange, endRange, AppointmentStatus.SCHEDULED);

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Seçilen saatte doktorun başka bir randevusu bulunmaktadır. Lütfen en az 30 dakika sonrasını deneyin.");
        }

        Patient patient = patientService.getPatientById(patientId).orElseThrow();
        Appointment appointment = new Appointment(patient, doctor, appointmentDate);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(appointment);
    }

    // 5. Randevu GÜNCELLE
    public Appointment updateAppointment(Long id, Appointment details) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı! ID: " + id));

        appointment.setAppointmentDate(details.getAppointmentDate());
        appointment.setStatus(details.getStatus());
        appointment.setNotes(details.getNotes());
        appointment.setComplaints(details.getComplaints());
        appointment.setDiagnosis(details.getDiagnosis());

        return appointmentRepository.save(appointment);
    }

    // 6. Randevu İPTAL ET
    public Appointment cancelAppointment(Long id) {
        Appointment app = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı!"));
        app.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(app);
    }

    // 7. Randevu TAMAMLA
    public Appointment completeAppointment(Long id) {
        Appointment app = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı!"));
        app.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(app);
    }

    public Appointment saveAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }
}