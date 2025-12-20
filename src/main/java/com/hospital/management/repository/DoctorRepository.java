package com.hospital.management.repository;

import com.hospital.management.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Kullanıcı adına göre doktor bul
    Optional<Doctor> findByUsername(String username);

    // Uzmanlık alanına göre doktorları listele
    List<Doctor> findBySpecialization(String specialization);

    // Departmana göre doktorları listele
    List<Doctor> findByDepartment(String department);

    // İsme göre doktor ara
    List<Doctor> findByFirstNameContainingIgnoreCase(String firstName);

    // Soyisme göre doktor ara
    List<Doctor> findByLastNameContainingIgnoreCase(String lastName);

    // Email'e göre doktor bul
    Optional<Doctor> findByEmail(String email);

    // Username var mı kontrol et
    boolean existsByUsername(String username);
}