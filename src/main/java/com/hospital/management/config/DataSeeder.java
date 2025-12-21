package com.hospital.management.config;

import com.hospital.management.model.Doctor;
import com.hospital.management.model.Secretary;
import com.hospital.management.repository.DoctorRepository;
import com.hospital.management.repository.SecretaryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final DoctorRepository doctorRepository;
    private final SecretaryRepository secretaryRepository;

    public DataSeeder(DoctorRepository doctorRepository, SecretaryRepository secretaryRepository) {
        this.doctorRepository = doctorRepository;
        this.secretaryRepository = secretaryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadDoctors();
        loadSecretaries();
    }

    private void loadDoctors() {
        String[][] doctorsData = {
                {"Mehmet", "Öz", "Kardiyoloji", "Kalp Sağlığı", "doktor1"},
                {"Canan", "Karatay", "Dahiliye", "İç Hastalıkları", "doktor2"},
                {"Oytun", "Erbaş", "Nöroloji", "Beyin ve Sinir", "doktor3"},
                {"Sabiha", "Gökçen", "Göz Hastalıkları", "Göz Kliniği", "doktor4"},
                {"Aziz", "Sancar", "Onkoloji", "Kanser Araştırma", "doktor5"}
        };

        for (String[] data : doctorsData) {
            if (doctorRepository.findByUsername(data[4]).isEmpty()) {
                Doctor d = new Doctor();
                d.setFirstName(data[0]);
                d.setLastName(data[1]);
                d.setSpecialization(data[2]);
                d.setDepartment(data[3]);
                d.setUsername(data[4]);
                d.setPassword("1234");
                doctorRepository.save(d);
            }
        }
    }

    private void loadSecretaries() {
        if (secretaryRepository.findByUsername("sekreter1").isEmpty()) {
            Secretary s = new Secretary();
            s.setFirstName("Selin");
            s.setLastName("Yılmaz");
            s.setUsername("sekreter1");
            s.setPassword("1234");
            secretaryRepository.save(s);
        }
    }
}