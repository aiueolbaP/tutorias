# Sistema de Gestión de Tutorías Académicas

Un sistema integral diseñado para facilitar la conexión, reserva y gestión de sesiones de tutoría académica entre estudiantes y tutores. 

## Funciones planteadas

* **Búsqueda Reactiva (Live Search):** Filtrado instantáneo de tutores por materia o disponibilidad impulsado por HTMX, actualizando resultados en tiempo real sin recargar la página.
* **Gestión de Roles:** Sistema de autenticación con perfiles diferenciados (Estudiante, Tutor, Administrador).
* **Sistema de Reservas:** Calendario interactivo que previene cruces de horarios y dobles reservas.
* **Panel de Control (Dashboard):** Vistas personalizadas donde los tutores pueden aceptar/rechazar solicitudes de forma asíncrona.
* **UI Moderna y Ligera:** Interfaces construidas con plantillas Thymeleaf y enriquecidas con HTMX para actualizaciones parciales del DOM (DOM swaps).

## Tecnologías

**Backend:**
* [Java 21](https://www.java.com/) - Lenguaje principal.
* [Spring Boot](https://spring.io/projects/spring-boot) - Framework central.
* **Spring Web MVC:** Arquitectura Modelo-Vista-Controlador.
* **Spring Data JPA / Hibernate:** ORM y persistencia de datos.
* **Spring Security:** Control de acceso, autenticación y encriptación de contraseñas.

**Frontend:**
* [Thymeleaf](https://www.thymeleaf.org/) - Motor de plantillas del lado del servidor.
* [HTMX](https://htmx.org/) - Interacciones asíncronas, peticiones AJAX y manipulación del DOM mediante atributos HTML. - Planteado


**Base de Datos:**
* PostgreSQL en una imagen de Docker.


**Crud y Login**
* https://youtu.be/GeaVkmUnCxs
