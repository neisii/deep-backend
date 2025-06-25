package org.example.backendproject;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SampleDataRunner implements CommandLineRunner {

    private final UserDataGenerator userDataGenerator;

    @Override
    public void run(String... args) {
        userDataGenerator.generateSampleUsers(3_000); // 1만 개 생성
    }
}

