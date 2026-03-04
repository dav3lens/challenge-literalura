package com.alura.literalura.service;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConsumoAPI {
    private static final Dotenv dotenv = Dotenv.load();
    // Leemos la URL desde el .env (ej: https://gutendex.com/books/)
    private final String URL_BASE = dotenv.get("GUTENDEX_URL");

    public String obtenerDatos(String nombreLibro) {
        HttpClient client = HttpClient.newHttpClient();

        // Construimos la URL usando la variable del .env
        // Reemplazamos espacios por %20 para que la URL sea válida
        String direccion = URL_BASE + "?search=" + nombreLibro.replace(" ", "%20");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(direccion))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // ✅ RETORNO 1: Si todo sale bien, devolvemos el cuerpo de la respuesta (el JSON)
            return response.body();
        } catch (IOException | InterruptedException e) {
            // En caso de error, lanzamos una excepción o devolvemos un String vacío
            System.out.println("❌ Error al conectar con la API: " + e.getMessage());
            // ✅ RETORNO 2: Devolvemos un JSON vacío o nulo para que el programa no truene
            return "";
        }
    }
}