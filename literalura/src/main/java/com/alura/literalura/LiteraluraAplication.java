package com.alura.literalura;

import com.alura.literalura.principal.MenuPrincipal;
import io.github.cdimascio.dotenv.Dotenv; // Importante añadir esta
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiteraluraAplication implements CommandLineRunner {

    public static void main(String[] args) {
        // 1. Cargamos el archivo .env manualmente
        Dotenv dotenv = Dotenv.load();

        // 2. Las inyectamos en las propiedades del sistema de Java
        // Esto permite que Spring las lea en application.properties usando ${VAR}
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USER", dotenv.get("DB_USER"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

        // Opcional: Si también quieres proteger la URL de Gutendex desde properties
        if (dotenv.get("GUTENDEX_URL") != null) {
            System.setProperty("GUTENDEX_URL", dotenv.get("GUTENDEX_URL"));
        }

        SpringApplication.run(LiteraluraAplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        MenuPrincipal menu = new MenuPrincipal();
        menu.ejecutar();
    }
}