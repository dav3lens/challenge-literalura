package com.alura.literalura.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    // Cargamos el .env una sola vez al iniciar la clase
    private static final Dotenv dotenv = Dotenv.load();

    public static Connection getConnection() throws SQLException {
        // Obtenemos los valores del .env
        return DriverManager.getConnection(
                dotenv.get("DB_URL"),
                dotenv.get("DB_USER"),
                dotenv.get("DB_PASSWORD")
        );
    }
}