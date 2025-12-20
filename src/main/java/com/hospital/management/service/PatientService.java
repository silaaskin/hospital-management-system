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

    // --- LOGİN İŞLEMİ ---
    public Optional<Patient> login(String tcNo) {
        return patientRepository.findByTcNo(tcNo);
    }

    // İsimle arama
    public List<Patient> searchPatientsByName(String name) {
        return patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
    }

    public Patient savePatient(Patient patient) {
        // TC No kontrolü yapılabilir (İsteğe bağlı)
        if (patientRepository.existsByTcNo(patient.getTcNo())) {
            // System.out.println("Bu TC zaten kayıtlı!");
        }
        return patientRepository.save(patient);
    }

    // --- DÜZELTİLEN METOT BURASI ---
    public Patient updatePatient(Long id, Patient patientDetails) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı ID: " + id));

        patient.setFirstName(patientDetails.getFirstName());
        patient.setLastName(patientDetails.getLastName());

        // DÜZELTME: setPhoneNumber yerine setPhone, getPhoneNumber yerine getPhone
        patient.setPhone(patientDetails.getPhone());

        patient.setTcNo(patientDetails.getTcNo());
        // Varsa adres gibi diğer alanları da buraya ekleyebilirsin:
        // patient.setAddress(patientDetails.getAddress());

        return patientRepository.save(patient);
    }

    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
}