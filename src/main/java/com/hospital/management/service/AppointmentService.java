package com.hospital.management.service;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Appointment.AppointmentStatus;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
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

    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        System.out.println("=== HASTA RANDEVULARI ÇEKİLİYOR ===");
        System.out.println("Patient ID: " + patientId);

        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı! ID: " + patientId));

        System.out.println("Hasta bulundu: " + patient.getFirstName() + " " + patient.getLastName() + " (TC: " + patient.getTcNo() + ")");

        List<Appointment> appointments = appointmentRepository.findByPatient(patient);
        System.out.println("Toplam randevu sayısı: " + appointments.size());

        // Her randevuyu logla
        for (Appointment a : appointments) {
            System.out.println("  - ID: " + a.getId() +
                    ", Tarih: " + a.getAppointmentDate() +
                    ", Durum: " + a.getStatus() +
                    ", Doktor: " + a.getDoctor().getFirstName());
        }

        return appointments;
    }

    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        System.out.println("=== DOKTOR RANDEVULARI ÇEKİLİYOR ===");
        System.out.println("Doctor ID: " + doctorId);

        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + doctorId));

        System.out.println("Doktor bulundu: Dr. " + doctor.getFirstName() + " " + doctor.getLastName());

        List<Appointment> appointments = appointmentRepository.findByDoctor(doctor);
        System.out.println("Toplam randevu sayısı: " + appointments.size());

        return appointments;
    }

    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    public List<Appointment> getAppointmentsInDateRange(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByAppointmentDateBetweenAndStatus(start, end, AppointmentStatus.SCHEDULED);
    }

    public List<Appointment> getUpcomingAppointmentsByPatient(Long patientId) {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı!"));
        return appointmentRepository.findUpcomingAppointmentsByPatient(patient, LocalDateTime.now());
    }

    // DÜZELTİLDİ: Çakışma Kontrolü İyileştirildi
    public Appointment createAppointment(Long patientId, Long doctorId, LocalDateTime appointmentDate) {
        System.out.println("=== YENİ RANDEVU OLUŞTURULUYOR ===");
        System.out.println("Patient ID: " + patientId);
        System.out.println("Doctor ID: " + doctorId);
        System.out.println("Tarih: " + appointmentDate);

        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Geçmiş tarih için randevu oluşturamazsınız!");
        }

        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı!"));

        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı!"));

        // DÜZELTİLDİ: 30 dakikalık çakışma kontrolü
        LocalDateTime startRange = appointmentDate.minusMinutes(29);
        LocalDateTime endRange = appointmentDate.plusMinutes(29);

        System.out.println("Çakışma kontrolü yapılıyor...");
        System.out.println("Kontrol aralığı: " + startRange + " - " + endRange);

        // Doktorun bu zaman dilimindeki SCHEDULED randevularını kontrol et
        List<Appointment> conflicts = appointmentRepository.findByDoctorAndAppointmentDateBetween(
                doctor, startRange, endRange);

        // Sadece SCHEDULED olanları filtrele
        conflicts = conflicts.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .toList();

        System.out.println("Çakışan randevu sayısı: " + conflicts.size());

        if (!conflicts.isEmpty()) {
            for (Appointment conflict : conflicts) {
                System.out.println("  ÇAKIŞMA! Randevu ID: " + conflict.getId() +
                        ", Tarih: " + conflict.getAppointmentDate() +
                        ", Durum: " + conflict.getStatus());
            }
            throw new RuntimeException("Bu saatte doktorun başka bir randevusu var! Lütfen en az 30 dakika sonrasını deneyin.");
        }

        Appointment appointment = new Appointment(patient, doctor, appointmentDate);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment saved = appointmentRepository.save(appointment);
        System.out.println("Randevu başarıyla oluşturuldu! ID: " + saved.getId());

        return saved;
    }

    public Appointment updateAppointment(Long id, Appointment details) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı! ID: " + id));

        appointment.setAppointmentDate(details.getAppointmentDate());
        appointment.setStatus(details.getStatus());
        return appointmentRepository.save(appointment);
    }

    public Appointment cancelAppointment(Long id) {
        Appointment app = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı!"));
        app.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(app);
    }

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