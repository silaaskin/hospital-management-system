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

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    public List<Doctor> getDoctorsByDepartment(String department) {
        return doctorRepository.findByDepartment(department);
    }

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

    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + id));

        doctor.setFirstName(doctorDetails.getFirstName());
        doctor.setLastName(doctorDetails.getLastName());
        doctor.setSpecialization(doctorDetails.getSpecialization());
        doctor.setDepartment(doctorDetails.getDepartment());
        doctor.setUsername(doctorDetails.getUsername());

        if (doctorDetails.getPassword() != null && !doctorDetails.getPassword().isEmpty()) {
            doctor.setPassword(doctorDetails.getPassword());
        }

        return doctorRepository.save(doctor);
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + id));
        doctorRepository.delete(doctor);
    }

    public Optional<Doctor> login(String username, String password) {
        Optional<Doctor> doctor = doctorRepository.findByUsername(username);
        if (doctor.isPresent() && doctor.get().getPassword().equals(password)) {
            return doctor;
        }
        return Optional.empty();
    }
}