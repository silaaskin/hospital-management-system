package com.hospital.management.service;

import com.hospital.management.model.Patient;
import com.hospital.management.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> getPatientByTcNo(String tcNo) {
        return patientRepository.findByTcNo(tcNo);
    }

    public Optional<Patient> login(String tcNo) {
        return patientRepository.findByTcNo(tcNo);
    }

    public List<Patient> searchPatientsByName(String name) {
        return patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
    }

    public Patient savePatient(Patient patient) {
        if (patient.getPhone() == null || patient.getPhone().trim().isEmpty()) {
            throw new RuntimeException("Telefon numarası boş bırakılamaz!");
        }

        String cleanPhone = patient.getPhone().replaceAll("\\D", "");
        if (cleanPhone.length() != 10) {
            throw new RuntimeException("Telefon numarası başında sıfır olmadan tam 10 hane olmalıdır (Örn: 533XXXXXXX)!");
        }
        patient.setPhone(cleanPhone);

        if (patient.getEmail() == null || patient.getEmail().trim().isEmpty()) {
            throw new RuntimeException("E-posta adresi boş bırakılamaz!");
        }

        if (patient.getBloodType() == null || patient.getBloodType().trim().isEmpty()) {
            throw new RuntimeException("Kan grubu seçimi zorunludur!");
        }

        if (patient.getId() == null && patientRepository.existsByTcNo(patient.getTcNo())) {
            throw new RuntimeException("Bu TC Kimlik numarası zaten kayıtlı!");
        }

        return patientRepository.save(patient);
    }

    public Patient updatePatient(Long id, Patient patientDetails) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı ID: " + id));

        patient.setFirstName(patientDetails.getFirstName());
        patient.setLastName(patientDetails.getLastName());
        patient.setPhone(patientDetails.getPhone());
        patient.setTcNo(patientDetails.getTcNo());
        patient.setAddress(patientDetails.getAddress());

        if (patientDetails.getEmail() != null) patient.setEmail(patientDetails.getEmail());
        if (patientDetails.getBloodType() != null) patient.setBloodType(patientDetails.getBloodType());
        if (patientDetails.getBirthDate() != null) patient.setBirthDate(patientDetails.getBirthDate());

        if (patientDetails.getPassword() != null && !patientDetails.getPassword().isEmpty()) {
            patient.setPassword(patientDetails.getPassword());
        }

        return patientRepository.save(patient);
    }

    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
}