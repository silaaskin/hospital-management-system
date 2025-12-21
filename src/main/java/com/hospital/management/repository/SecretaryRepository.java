package com.hospital.management.repository;

import com.hospital.management.model.Secretary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecretaryRepository extends JpaRepository<Secretary, Long> {
    // Kullanıcı adına göre sekreter bulmak için gerekli
    Optional<Secretary> findByUsername(String username);
}