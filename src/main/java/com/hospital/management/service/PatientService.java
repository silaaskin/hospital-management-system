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

    // Tüm hastaları listele
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    // ID'ye göre hasta bul
    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    // TC No'ya göre hasta bul
    public Optional<Patient> getPatientByTcNo(String tcNo) {
        return patientRepository.findByTcNo(tcNo);
    }

    // Yeni hasta kaydet
    public Patient savePatient(Patient patient) {
        // TC No kontrolü
        if (patient.getTcNo() != null && patientRepository.existsByTcNo(patient.getTcNo())) {
            if (patient.getId() == null) {
                throw new RuntimeException("Bu TC Kimlik No ile kayıtlı hasta zaten mevcut!");
            }
            // Güncelleme işleminde mevcut hastanın TC'si kontrol edilir
            Optional<Patient> existing = patientRepository.findByTcNo(patient.getTcNo());
            if (existing.isPresent() && !existing.get().getId().equals(patient.getId())) {
                throw new RuntimeException("Bu TC Kimlik No başka bir hasta tarafından kullanılıyor!");
            }
        }
        return patientRepository.save(patient);
    }

    // Hasta güncelle
    public Patient updatePatient(Long id, Patient patientDetails) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı! ID: " + id));

        patient.setFirstName(patientDetails.getFirstName());
        patient.setLastName(patientDetails.getLastName());
        patient.setTcNo(patientDetails.getTcNo());
        patient.setBirthDate(patientDetails.getBirthDate());
        patient.setPhone(patientDetails.getPhone());
        patient.setEmail(patientDetails.getEmail());
        patient.setAddress(patientDetails.getAddress());
        patient.setBloodType(patientDetails.getBloodType());

        return patientRepository.save(patient);
    }

    // Hasta sil
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı! ID: " + id));
        patientRepository.delete(patient);
    }

    // İsme göre hasta ara
    public List<Patient> searchPatientsByName(String name) {
        return patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
    }

    // Email'e göre hasta bul
    public Optional<Patient> getPatientByEmail(String email) {
        return patientRepository.findByEmail(email);
    }

    // Telefona göre hasta bul
    public Optional<Patient> getPatientByPhone(String phone) {
        return patientRepository.findByPhone(phone);
    }
}