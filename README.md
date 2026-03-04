# 📚 LiterAlura - Catálogo de Libros con Spring Boot & JDBC

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.3-brightgreen?style=for-the-badge&logo=spring-boot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-JSONB-blue?style=for-the-badge&logo=postgresql)

**LiterAlura** es una robusta aplicación de consola que permite la gestión de una biblioteca personal interactuando con la API de [Gutendex](https://gutendex.com/). El proyecto destaca por su arquitectura orientada a la seguridad y el uso eficiente de persistencia de datos complejos.

## 🚀 Características Técnicas

* **Búsqueda Dinámica:** Consumo de API REST con manejo de parámetros de búsqueda para libros y autores.
* **Persistencia Híbrida (JSONB):** Uso avanzado de **PostgreSQL JSONB** para almacenar estructuras de datos flexibles (Records de Java) en una sola columna, permitiendo consultas SQL directas sobre el JSON.
* **Seguridad (Dotenv):** Implementación de variables de entorno para proteger credenciales sensibles (Base de Datos y URL de API), evitando fugas de información en el repositorio.
* **Lógica de Análisis:** Generación de reportes estadísticos utilizando **Java Streams** y `DoubleSummaryStatistics` (promedios, máximos y mínimos de descargas).
* **UI en Consola:** Interfaz interactiva con códigos de colores ANSI para una mejor experiencia de usuario en terminales con temas oscuros.

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 25 (JDK 25)
* **Framework:** Spring Boot 4.x (CommandLineRunner)
* **Base de Datos:** PostgreSQL
* **Librerías Clave:**
  * `Jackson`: Mapeo de JSON a Records.
  * `Dotenv-java`: Gestión de variables de entorno.
  * `JDBC`: Conexión y gestión de transacciones.

---

## 🎮 Funcionalidades del Menú

La aplicación incluye un menú interactivo con las siguientes capacidades:

* **🔍 Buscar y Agregar Libro:** Consulta la API de Gutendex y persiste el resultado en la base de datos de forma automática.
* **📊 Estadísticas:** Reporte detallado de descargas y resumen general de la biblioteca utilizando *Java Streams*.
* **📖 Mi Colección:** Listado completo y ordenado de todos los libros guardados en la base de datos local.
* **🗑️ Eliminación Masiva:** Sistema eficiente para borrar múltiples registros simultáneamente mediante índices de lista.
* **🌐 Filtro por Idioma:** Búsqueda segmentada por códigos internacionales (es, en, fr, pt, etc.).
* **✍️ Autores Vivos:** Filtrado inteligente de autores basados en un año específico de la historia, consultando directamente sobre el campo JSONB.

---

## ✒️ Autor

* **David**

---

## 📋 Configuración del Proyecto

### 1. Requisitos Previos
* PostgreSQL instalado y funcionando.
* Una base de datos llamada `literalura`.

### 2. Variables de Entorno
Crea un archivo `.env` en la raíz del proyecto (al mismo nivel que `pom.xml`) e incluye tus credenciales:

```env
DB_URL=jdbc:postgresql://localhost:5432/literalura
DB_USER=tu_usuario_postgres
DB_PASSWORD=tu_password_seguro
GUTENDEX_URL=[https://gutendex.com/books/](https://gutendex.com/books/)
