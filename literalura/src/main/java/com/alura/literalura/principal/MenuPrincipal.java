package com.alura.literalura.principal;

import com.alura.literalura.modelo.RespuestaAPI;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Scanner;

public class MenuPrincipal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();
    private LibroRepository repositorio = new LibroRepository();
    private ObjectMapper mapper = new ObjectMapper();

    // CONSTANTES PARA COLORES ANSI
    private final String RESET = "\u001B[0m";
    private final String ROJO = "\u001B[31m";
    private final String VERDE = "\u001B[32m";
    private final String AMARILLO = "\u001B[33m";
    private final String AZUL = "\u001B[34m";
    private final String CYAN = "\u001B[36m";

    public void ejecutar() {
        System.out.println(CYAN + "Sincronizando base de datos..." + RESET);
        repositorio.crearTablaSiNoExiste();

        int opcion = -1;
        while (opcion != 0) {
            System.out.print(AZUL + """
                \n====================================
                |     MENU PRINCIPAL LITERALURA    |
                ====================================
                """ + RESET + """
                1 - 🔍 Buscar y Agregar Libro
                2 - 📊 Estadísticas y Resumen
                3 - 📖 Ver mi Colección
                4 - 🗑️ Eliminar Libro (por número de lista)
                5 - 🌐 Filtrar Libros por Idioma
                6 - ✍️ Buscar Autores Vivos en Año "X"
                0 - Salir
                """ + AZUL + "Selección: " + RESET);

            opcion = leerEntero();
            switch (opcion) {
                case 1 -> buscarEnWeb();
                case 2 -> menuEstadisticas();
                case 3 -> repositorio.consultarDB();
                case 4 -> eliminarLibroConIndice();
                case 5 -> filtrarPorIdioma();
                case 6 -> buscarAutoresPorAnio();
                case 0 -> System.out.println(AMARILLO + "Cerrando aplicación..." + RESET);
                default -> System.out.println(ROJO + "Opción no válida." + RESET);
            }
        }
    }

    private void filtrarPorIdioma() {
        List<String> idiomasValidos = List.of("es", "en", "fr", "pt", "it", "de");
        System.out.println("\n🌐 Idiomas soportados: " + CYAN + idiomasValidos + RESET);
        System.out.print("Ingrese el código del idioma: ");
        String lang = teclado.nextLine().toLowerCase().trim();

        if (idiomasValidos.contains(lang)) {
            repositorio.consultarPorIdioma(lang);
        } else {
            System.out.println(ROJO + "❌ Código de idioma no reconocido." + RESET);
        }
    }

    private void buscarAutoresPorAnio() {
        System.out.print("\n¿Qué año desea consultar? ");
        int anio = leerEntero();
        if (anio > 0) {
            repositorio.autoresVivosEnAnio(anio);
        } else {
            System.out.println(AMARILLO + "⚠️ Por favor, ingrese un año válido." + RESET);
        }
    }

    private void eliminarLibroConIndice() {
        List<Integer> idsActuales = repositorio.consultarDB();
        if (idsActuales.isEmpty()) return;

        System.out.println(AMARILLO + "Escriba los números [#] separados por comas para eliminar (ej: 1, 3, 5) o 0 para cancelar:" + RESET);
        String entrada = teclado.nextLine(); // Leemos toda la línea

        if (entrada.equals("0") || entrada.trim().isEmpty()) {
            System.out.println("❌ Operación cancelada.");
            return;
        }

        // Dividimos la entrada por comas
        String[] indicesStr = entrada.split(",");

        System.out.print(ROJO + "¿Está seguro de que desea eliminar estos " + indicesStr.length + " libros? (S/N): " + RESET);
        String confirmacion = teclado.nextLine();

        if (confirmacion.equalsIgnoreCase("s")) {
            int eliminados = 0;
            for (String s : indicesStr) {
                try {
                    int indice = Integer.parseInt(s.trim());
                    if (indice > 0 && indice <= idsActuales.size()) {
                        int idReal = idsActuales.get(indice - 1);
                        repositorio.eliminarLibroPorId(idReal);
                        eliminados++;
                    } else {
                        System.out.println(AMARILLO + "⚠️ El índice " + indice + " no es válido y será ignorado." + RESET);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ROJO + "⚠️ '" + s.trim() + "' no es un número válido." + RESET);
                }
            }
            System.out.println(VERDE + "✅ Proceso terminado. Libros eliminados: " + eliminados + RESET);
        } else {
            System.out.println("❌ Eliminación masiva cancelada.");
        }
    }

    private void buscarEnWeb() {
        System.out.println(CYAN + "Escribe el nombre del libro o autor:" + RESET);
        String busqueda = teclado.nextLine();

        // Ahora 'consumo' ya sabe la URL base gracias al archivo .env
        String json = consumo.obtenerDatos(busqueda);

        try {
            RespuestaAPI datos = mapper.readValue(json, RespuestaAPI.class);
            if (datos.resultados().isEmpty()) {
                System.out.println(ROJO + "❌ No se encontró nada en la web." + RESET);
                return;
            }

            System.out.println("\n--- " + VERDE + "RESULTADOS" + RESET + " ---");
            // Mostramos los primeros 5 resultados
            for (int i = 0; i < Math.min(5, datos.resultados().size()); i++) {
                System.out.println(CYAN + (i + 1) + RESET + " - " + datos.resultados().get(i).titulo());
            }

            System.out.print(AMARILLO + "Selecciona el número para guardar (0 para cancelar): " + RESET);
            int sel = leerEntero();
            if (sel > 0 && sel <= datos.resultados().size()) {
                // Se envía el Record DatosLibro al repositorio
                repositorio.guardarLibro(datos.resultados().get(sel - 1));
            }
        } catch (Exception e) {
            System.out.println(ROJO + "Error al procesar: " + e.getMessage() + RESET);
        }
    }

    private void menuEstadisticas() {
        System.out.println("\n" + CYAN + "1- Resumen General | 2- Top 5 Descargas | 3- Estadísticas de Descargas (Streams) | 4- Volver" + RESET);
        int sel = leerEntero();
        switch (sel) {
            case 1 -> repositorio.mostrarResumenGeneral();
            case 2 -> repositorio.mostrarTop5();
            case 3 -> repositorio.mostrarEstadisticas(); // El método que usa DoubleSummaryStatistics
            default -> {}
        }
    }

    private int leerEntero() {
        try {
            int n = teclado.nextInt();
            teclado.nextLine();
            return n;
        } catch (Exception e) {
            teclado.nextLine();
            return -1;
        }
    }
}