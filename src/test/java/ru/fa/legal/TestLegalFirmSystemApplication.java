package ru.fa.legal;

import org.springframework.boot.SpringApplication;

public class TestLegalFirmSystemApplication {

    public static void main(String[] args) {
        SpringApplication.from(LegalFirmApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
