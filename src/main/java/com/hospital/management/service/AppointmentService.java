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

    // Tüm randevuları listele
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // ID'ye göre randevu bul
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

    // Duruma göre randevuları listele
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    // Yeni randevu oluştur
    public Appointment createAppointment(Long patientId, Long doctorId, LocalDateTime appointmentDate) {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı! ID: " + patientId));

        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + doctorId));

        // Geçmiş tarih kontrolü
        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Geçmiş tarih için randevu oluşturamazsınız!");
        }

        Appointment appointment = new Appointment(patient, doctor, appointmentDate);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        return appointmentRepository.save(appointment);
    }

    // Randevu kaydet/güncelle
    public Appointment saveAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    // Randevu güncelle
    public Appointment updateAppointment(Long id, Appointment appointmentDetails) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı! ID: " + id));

        appointment.setAppointmentDate(appointmentDetails.getAppointmentDate());
        appointment.setStatus(appointmentDetails.getStatus());
        appointment.setNotes(appointmentDetails.getNotes());
        appointment.setComplaints(appointmentDetails.getComplaints());
        appointment.setDiagnosis(appointmentDetails.getDiagnosis());

        return appointmentRepository.save(appointment);
    }

    // Randevu iptal et
    public Appointment cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı! ID: " + id));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(appointment);
    }

    // Randevu tamamla
    public Appointment completeAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı! ID: " + id));

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }

    // Randevu sil
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı! ID: " + id));
        appointmentRepository.delete(appointment);
    }

    // Hastanın gelecek randevularını getir
    public List<Appointment> getUpcomingAppointmentsByPatient(Long patientId) {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı! ID: " + patientId));
        return appointmentRepository.findUpcomingAppointmentsByPatient(patient, LocalDateTime.now());
    }

    // Doktorun gelecek randevularını getir
    public List<Appointment> getUpcomingAppointmentsByDoctor(Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + doctorId));
        return appointmentRepository.findUpcomingAppointmentsByDoctor(doctor, LocalDateTime.now());
    }

    // Belirli tarih aralığındaki randevuları getir
    public List<Appointment> getAppointmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByAppointmentDateBetween(startDate, endDate);
    }
}