package com.alura.literalura.repository;

import com.alura.literalura.config.DatabaseConfig;
import com.alura.literalura.modelo.DatosAutor;

import com.alura.literalura.modelo.DatosLibro;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class LibroRepository {

        private final String RESET = "\u001B[0m";
        private final String ROJO = "\u001B[31m";
        private final String VERDE = "\u001B[32m";
        private final String AMARILLO = "\u001B[33m";
        private final String AZUL = "\u001B[34m";
        private final String CYAN = "\u001B[36m";

        private ObjectMapper mapper = new ObjectMapper();

    // 1. INICIALIZACIÓN DE TABLA
    public void crearTablaSiNoExiste() {
        String sql = """
            CREATE TABLE IF NOT EXISTS libros_registrados (
                id SERIAL PRIMARY KEY,
                titulo TEXT,
                guttenberg_id INTEGER UNIQUE,
                datos_completos JSONB
            );
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("❌ Error al inicializar la tabla: " + e.getMessage());
        }
    }

    // 2. GUARDAR LIBRO
    public void guardarLibro(DatosLibro libro) {
        // Consultas SQL preparadas
        String sqlCheck = "SELECT COUNT(*) FROM libros_registrados WHERE guttenberg_id = ?";
        String sqlInsert = "INSERT INTO libros_registrados (titulo, guttenberg_id, datos_completos) VALUES (?, ?, ?::jsonb)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            // 1. Aseguramos que la base de datos guarde los cambios inmediatamente
            conn.setAutoCommit(true);

            // 2. Verificación de duplicados para evitar errores de llave única (UNIQUE)
            PreparedStatement check = conn.prepareStatement(sqlCheck);
            check.setInt(1, libro.idGutenberg());
            ResultSet rs = check.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println(AMARILLO + "⚠️ El libro '" + libro.titulo() + "' ya está en tu colección." + RESET);
                return;
            }

            // 3. Inserción del nuevo libro
            PreparedStatement insert = conn.prepareStatement(sqlInsert);
            insert.setString(1, libro.titulo());
            insert.setInt(2, libro.idGutenberg());

            // Convertimos el Record DatosLibro completo a un String JSON para la columna JSONB
            // Esto guarda automáticamente la lista de autores con sus años
            String jsonCompleto = mapper.writeValueAsString(libro);
            insert.setString(3, jsonCompleto);

            int filasAfectadas = insert.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println(VERDE + "✅ ¡Éxito! Guardado en la base de datos: " + libro.titulo() + RESET);
            }

        } catch (SQLException e) {
            System.out.println(ROJO + "❌ Error de SQL: " + e.getMessage() + RESET);
        } catch (Exception e) {
            System.out.println(ROJO + "❌ Error al procesar JSON: " + e.getMessage() + RESET);
        }
    }

    // 3. CONSULTAR COLECCIÓN (Devuelve lista de IDs para el menú)
    public List<Integer> consultarDB() {
        List<Integer> idsGutenberg = new ArrayList<>();
        String sql = """
        SELECT guttenberg_id, titulo, 
               datos_completos->'autores'->0->>'nombre' as autor,
               datos_completos->'idiomas'->0 as idioma
        FROM libros_registrados
        ORDER BY titulo ASC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {

            System.out.println("\n" + "═".repeat(60));
            System.out.println("           📚 TU COLECCIÓN DE LIBROS REGISTRADOS");
            System.out.println("═".repeat(60));

            int contador = 1;
            while (rs.next()) {
                int idReal = rs.getInt("guttenberg_id");
                idsGutenberg.add(idReal);

                String titulo = rs.getString("titulo");
                String autor = rs.getString("autor") != null ? rs.getString("autor") : "Autor desconocido";
                String idioma = rs.getString("idioma") != null ? rs.getString("idioma").replace("\"", "") : "N/A";

                System.out.printf("[%d] %-35s | %-20s | [%s]\n",
                        contador, titulo, autor, idioma.toUpperCase());
                contador++;
            }

            if (idsGutenberg.isEmpty()) System.out.println("   La biblioteca está vacía.");
            System.out.println("═".repeat(60));

        } catch (SQLException e) {
            System.out.println("❌ Error al consultar la colección: " + e.getMessage());
        }
        return idsGutenberg;
    }

    // 4. FILTRADO POR IDIOMA (Mejorado con validación JSONB)
    public void consultarPorIdioma(String idioma) {
        String sql = """
            SELECT titulo, datos_completos->'autores'->0->>'nombre' as autor 
            FROM libros_registrados 
            WHERE datos_completos->'idiomas' @> ?::jsonb
    """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "[\"" + idioma + "\"]");
            ResultSet rs = ps.executeQuery();

            System.out.println("\n" + "═".repeat(50));
            System.out.println("       🌐 RESULTADOS EN IDIOMA: " + idioma.toUpperCase());
            System.out.println("═".repeat(50));

            boolean encontrado = false;
            while (rs.next()) {
                encontrado = true;
                System.out.printf("• %-35s | Autor: %s\n",
                        rs.getString("titulo"), rs.getString("autor"));
            }
            if (!encontrado) System.out.println("   No hay libros registrados en ese idioma.");
            System.out.println("═".repeat(50));

        } catch (SQLException e) {
            System.out.println("❌ Error al filtrar: " + e.getMessage());
        }
    }

    // 5. AUTORES VIVOS EN DETERMINADO AÑO (Mejorado con manejo de Nulos)
    public void autoresVivosEnAnio(int anio) {
        String sql = """
    SELECT DISTINCT 
        elem->>'nombre' as nombre_autor,
        (elem->>'nacimiento')::int as anio_nacimiento,
        (elem->>'fallecimiento')::int as anio_fallecimiento
    FROM libros_registrados, 
         jsonb_array_elements(datos_completos->'autores') elem
    WHERE (elem->>'nacimiento')::int <= ? 
      AND ((elem->>'fallecimiento')::int >= ? OR elem->>'fallecimiento' IS NULL)
    ORDER BY nombre_autor ASC
    """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, anio);
            ps.setInt(2, anio);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n" + "╔" + "═".repeat(48) + "╗");
            System.out.printf("║      ✍️  AUTORES VIVOS EN EL AÑO %-14d ║\n", anio);
            System.out.println("╚" + "═".repeat(48) + "╝");

            boolean hayAutores = false;
            while (rs.next()) {
                hayAutores = true;
                String fallecimiento = rs.getString("anio_fallecimiento");
                System.out.printf(" • %-25s | Período: %d - %s\n",
                        rs.getString("nombre_autor"),
                        rs.getInt("anio_nacimiento"),
                        (fallecimiento == null || fallecimiento.equals("0")) ? "Presente" : fallecimiento);
            }

            if (!hayAutores) {
                System.out.println("   No se encontraron autores vivos en ese año en tu colección.");
            }
            System.out.println("═".repeat(50));

        } catch (SQLException e) {
            System.out.println(ROJO + "❌ Error al consultar autores: " + e.getMessage() + RESET);
            System.out.println("Tip: Asegúrate de que los libros guardados tengan años válidos.");
        }
    }

    // 6. RESUMEN GENERAL
    public void mostrarResumenGeneral() {
        String sql = """
        SELECT COUNT(*) as total_libros, 
               COUNT(DISTINCT datos_completos->'autores'->0->>'nombre') as total_autores,
               SUM((datos_completos->>'descargas')::int) as total_descargas
        FROM libros_registrados
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            if (rs.next()) {
                System.out.println("""
                
                ╔══════════════════════════════════════════════════╗
                ║           📊 RESUMEN DE TU BIBLIOTECA            ║
                ╠══════════════════════════════════════════════════╣
                ║  📚 Libros registrados:      %-18d  ║
                ║  ✍️ Autores únicos:          %-18d  ║
                ║  🚀 Descargas totales:       %-18d  ║
                ╚══════════════════════════════════════════════════╝
                """.formatted(rs.getInt("total_libros"), rs.getInt("total_autores"), rs.getLong("total_descargas")));
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al generar el resumen.");
        }
    }

    // 7. TOP 5
    public void mostrarTop5() {
        // Cambiamos 'download_count' por 'descargas' para que coincida con tu Record
        String sql = """
        SELECT titulo, (datos_completos->>'descargas')::int as total_descargas
        FROM libros_registrados
        ORDER BY total_descargas DESC LIMIT 5
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {

            System.out.println("\n⭐ TOP 5 LIBROS MÁS DESCARGADOS ⭐");
            int puesto = 1;
            while (rs.next()) {
                // Usamos el alias 'total_descargas' que definimos arriba
                System.out.printf("%d. [%-7d descargas] - %s\n",
                        puesto++, rs.getInt("total_descargas"), rs.getString("titulo"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al consultar el Top 5: " + e.getMessage());
        }
    }

    // 8. ELIMINAR
    public void eliminarLibroPorId(int guttenbergId) {
        String sql = "DELETE FROM libros_registrados WHERE guttenberg_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guttenbergId);
            if (ps.executeUpdate() > 0) System.out.println("✅ Libro eliminado.");
        } catch (SQLException e) {
            System.out.println("❌ Error al eliminar.");
        }
    }

    public void mostrarEstadisticas() {
        // Extraemos el número de descargas del JSON y lo convertimos a entero
        String sql = "SELECT (datos_completos->>'descargas')::int as descargas FROM libros_registrados";
        List<Integer> todasLasDescargas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                todasLasDescargas.add(rs.getInt("descargas"));
            }

            if (todasLasDescargas.isEmpty()) {
                System.out.println("\n📊 No hay suficientes datos para generar estadísticas.");
                return;
            }

            // --- LA MAGIA DE JAVA STREAMS ---
            DoubleSummaryStatistics stats = todasLasDescargas.stream()
                    .mapToDouble(x -> x)
                    .summaryStatistics();

            System.out.println("\n" + "╔" + "═".repeat(48) + "╗");
            System.out.println("║       📊 RESUMEN ESTADÍSTICO DE DESCARGAS      ║");
            System.out.println("╚" + "═".repeat(48) + "╝");
            System.out.printf(" • Cantidad de libros: %d\n", stats.getCount());
            System.out.printf(" • Total de descargas: %,.0f\n", stats.getSum());
            System.out.printf(" • Promedio de descargas: %,.2f\n", stats.getAverage());
            System.out.printf(" • Máximo de descargas: %,.0f\n", stats.getMax());
            System.out.printf(" • Mínimo de descargas: %,.0f\n", stats.getMin());
            System.out.println("═".repeat(50));

        } catch (SQLException e) {
            System.out.println("❌ Error al generar estadísticas: " + e.getMessage());
        }
    }
}