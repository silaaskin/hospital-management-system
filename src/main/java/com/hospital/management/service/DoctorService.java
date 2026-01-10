package com.hospital.management.service;

import com.hospital.management.model.Doctor;
import com.hospital.management.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    // Tüm doktorları listele
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // ID'ye göre doktor bul
    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    // Uzmanlık alanına göre doktorları listele
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    // Departmana göre doktorları listele
    public List<Doctor> getDoctorsByDepartment(String department) {
        return doctorRepository.findByDepartment(department);
    }

    // Yeni doktor kaydet
    public Doctor saveDoctor(Doctor doctor) {
        if (doctor.getUsername() != null && doctorRepository.existsByUsername(doctor.getUsername())) {
            if (doctor.getId() == null) {
                throw new RuntimeException("Bu kullanıcı adı zaten kullanılıyor!");
            }
            Optional<Doctor> existing = doctorRepository.findByUsername(doctor.getUsername());
            if (existing.isPresent() && !existing.get().getId().equals(doctor.getId())) {
                throw new RuntimeException("Bu kullanıcı adı başka bir doktor tarafından kullanılıyor!");
            }
        }
        return doctorRepository.save(doctor);
    }

    // Doktor güncelle
    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + id));

        doctor.setFirstName(doctorDetails.getFirstName());
        doctor.setLastName(doctorDetails.getLastName());
        doctor.setSpecialization(doctorDetails.getSpecialization());
        doctor.setDepartment(doctorDetails.getDepartment());
        doctor.setUsername(doctorDetails.getUsername());

        // Şifre sadece dolu gönderildiyse güncelle
        if (doctorDetails.getPassword() != null && !doctorDetails.getPassword().isEmpty()) {
            doctor.setPassword(doctorDetails.getPassword());
        }

        return doctorRepository.save(doctor);
    }

    // Doktor sil
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + id));
        doctorRepository.delete(doctor);
    }

    // Doktor girişi
    public Optional<Doctor> login(String username, String password) {
        Optional<Doctor> doctor = doctorRepository.findByUsername(username);
        if (doctor.isPresent() && doctor.get().getPassword().equals(password)) {
            return doctor;
        }
        return Optional.empty();
    }
}