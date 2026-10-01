package com.cti.provedor;

import org.springframework.boot.SpringApplication;

/**
 * Sobe a API com o banco H2 em memória das configurações de teste, sem tocar no PostgreSQL.
 * Uso: ./mvnw spring-boot:test-run (os dados somem ao parar).
 */
public class TestCtiProvedorApplication {

    public static void main(String[] args) {
        SpringApplication.from(CtiProvedorApplication::main).run(args);
    }
}
