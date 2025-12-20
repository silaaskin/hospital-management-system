package com.hospital.management.repository;

import com.hospital.management.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // TC Kimlik No'ya göre hasta bul (Giriş işlemi için kritik)
    Optional<Patient> findByTcNo(String tcNo);

    // İsme göre hasta ara
    List<Patient> findByFirstNameContainingIgnoreCase(String firstName);

    // Soyisme göre hasta ara
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);

    // İsim veya soyisme göre hasta ara
    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // Email'e göre hasta bul (Eğer modelinde email varsa kullanılır, yoksa hata verebilir, kontrol et)
    // Optional<Patient> findByEmail(String email);

    // Telefona göre hasta bul
    // Optional<Patient> findByPhone(String phone);

    // TC No var mı kontrol et
    boolean existsByTcNo(String tcNo);
}