package com.hospital.management.config;

import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.DoctorRepository;
import com.hospital.management.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public DataSeeder(DoctorRepository doctorRepository, PatientRepository patientRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadDoctors();
        loadPatients();
    }

    private void loadDoctors() {
        if (doctorRepository.count() == 0) {
            Doctor d1 = new Doctor();
            d1.setFirstName("Mehmet");
            d1.setLastName("Öz");
            d1.setSpecialization("Kardiyoloji");
            d1.setDepartment("Kalp Damar");
            d1.setUsername("doktor1");
            d1.setPassword("1234"); // Gerçek hayatta şifreli olmalı!

            Doctor d2 = new Doctor();
            d2.setFirstName("Canan");
            d2.setLastName("Karatay");
            d2.setSpecialization("Dahiliye");
            d2.setDepartment("İç Hastalıkları");
            d2.setUsername("doktor2");
            d2.setPassword("1234");

            doctorRepository.save(d1);
            doctorRepository.save(d2);
            System.out.println("✅ Örnek Doktorlar Eklendi!");
        }
    }

    private void loadPatients() {
        if (patientRepository.count() == 0) {
            Patient p1 = new Patient();
            p1.setFirstName("Ahmet");
            p1.setLastName("Yılmaz");
            p1.setTcNo("11111111111");
            p1.setPhone("5551234567");
            p1.setBirthDate(LocalDate.of(1990, 5, 20));
            p1.setAddress("İstanbul, Kadıköy");
            p1.setBloodType("A+");

            Patient p2 = new Patient();
            p2.setFirstName("Ayşe");
            p2.setLastName("Demir");
            p2.setTcNo("22222222222");
            p2.setPhone("5559876543");
            p2.setBirthDate(LocalDate.of(1985, 8, 15));
            p2.setAddress("Ankara, Çankaya");
            p2.setBloodType("0-");

            patientRepository.save(p1);
            patientRepository.save(p2);
            System.out.println("✅ Örnek Hastalar Eklendi!");
        }
    }
}