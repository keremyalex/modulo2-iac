# Backend — Sistema de Control y Registro de Asistencia Estudiantil

API REST para la gestión de asistencia de estudiantes de un diplomado.

**Stack:** Java 17 · Spring Boot 3.3.4 · Spring Data JPA · PostgreSQL 16 · Apache POI 5.2.5

---

## Requisitos

- Java 17 o superior
- Maven 3.9+
- PostgreSQL 16 (o Docker para levantarlo con el archivo `docker-compose.yaml` de la raíz del repo)

## Puesta en marcha

Desde la **raíz del repo**:

```bash
# 1. Crear el archivo de entorno con tus credenciales de base de datos
cp .env.example .env

# 2. Levantar PostgreSQL
docker compose up -d postgres

# 3. Arrancar el backend (desde la carpeta backend/)
cd backend
mvn spring-boot:run
```

El servidor queda disponible en `http://localhost:8080`.

Para compilar el jar:

```bash
mvn clean package
java -jar target/backend-1.0.0.jar
```

## Configuración

La conexión a la base de datos se define en `backend/src/main/resources/application.properties`:

| Propiedad | Descripción |
|---|---|
| `spring.datasource.url` | Variable `SPRING_DATASOURCE_URL` (por defecto `jdbc:postgresql://localhost:5432/asistenciadiplomado`) |
| `spring.datasource.username` | Variable `SPRING_DATASOURCE_USERNAME` (por defecto `globomatics_app`) |
| `spring.datasource.password` | Variable obligatoria `SPRING_DATASOURCE_PASSWORD` |
| `server.port` | Puerto del servidor (8080) |
| `spring.jpa.hibernate.ddl-auto` | `update` — el schema lo gestiona Hibernate (sin Flyway) |

Las credenciales del contenedor Postgres se toman del `.env` (ver `.env.example`).

## Estructura del proyecto

```
backend/src/main/java/com/diplomado/asistencia/
├── AsistenciaApplication.java   # Punto de entrada
├── config/                      # CORS, datos de demostración (DataInitializer)
├── controller/                  # Endpoints REST
├── dto/                         # Objetos de transferencia
├── entity/                      # Entidades JPA
├── exception/                   # Manejador global de errores
├── repository/                  # Interfaces Spring Data JPA
└── service/                     # Lógica de negocio
```

## Modelo de datos

| Entidad | Descripción |
|---|---|
| `Modulo` | Curso/diplomado; solo uno puede estar `activo` a la vez |
| `Estudiante` | Inscripto en un módulo (`numeroRegistro`, nombre, correo) |
| `FechaClase` | Sesión de clase de un módulo (fecha, hora inicio/fin) |
| `Asistencia` | Registro por estudiante y fecha de clase: `PRESENTE`, `FALTA` o `JUSTIFICADA` |
| `AuditoriaAsistencia` | Trazabilidad inmutable de toda modificación de asistencia |
| `Actividad` | Bitácora de eventos del sistema (logins, importaciones, etc.) |

## API

Todas las respuestas usan el envoltorio `ApiResponse` (`success`, `message`, `data`).

### Autenticación — `/api/auth`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/estudiante/login` | Login de estudiante por número de registro o correo |
| POST | `/api/auth/admin/login` | Login de administrador (usuario/contraseña) |

### Portal del estudiante — `/api/estudiante`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/estudiante/{estudianteId}/resumen` | Resumen: datos del estudiante, clase de hoy e historial |
| POST | `/api/estudiante/marcar` | Registra la asistencia del estudiante en la clase de hoy |

### Administrador — `/api/admin`

**Módulos**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/modulos` | Lista todos los módulos |
| GET | `/modulos/activo` | Módulo activo actual |
| POST | `/modulos/activar-nuevo?usuario=` | Crea un módulo y lo activa |
| POST | `/modulos/{id}/activar?usuario=` | Activa un módulo existente |
| POST | `/modulos/reiniciar?usuario=` | Reinicia el módulo activo (motivo obligatorio) |
| GET | `/modulos/{moduloId}/dashboard` | Estadísticas KPI del módulo |

**Estudiantes**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/modulos/{moduloId}/estudiantes?q=` | Lista estudiantes con búsqueda opcional |
| POST | `/modulos/{moduloId}/estudiantes` | Crea un estudiante |

**Fechas de clase**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/modulos/{moduloId}/fechas` | Lista las fechas de clase |
| POST | `/modulos/{moduloId}/fechas` | Crea una fecha de clase |

**Asistencia**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/modulos/{moduloId}/matriz?fechaId=&q=` | Matriz estudiantes × fechas |
| PUT | `/asistencias/modificar` | Modifica un estado (auditoría con motivo obligatorio) |
| POST | `/modulos/{moduloId}/sincronizar` | Sincroniza faltas para fechas pasadas |

**Auditoría y actividades**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/modulos/{moduloId}/auditorias` | Auditorías del módulo |
| GET | `/auditorias` | Todas las auditorías |
| GET | `/modulos/{moduloId}/actividades` | Actividades del módulo |
| GET | `/actividades` | Todas las actividades |

**Excel**

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/excel/preview-estudiantes` | Previsualiza importación de estudiantes |
| POST | `/excel/preview-fechas` | Previsualiza importación de fechas |
| POST | `/modulos/{moduloId}/importar-estudiantes?usuario=` | Importa estudiantes desde Excel |
| POST | `/modulos/{moduloId}/importar-fechas?usuario=` | Importa fechas desde Excel |
| GET | `/modulos/{moduloId}/exportar-excel` | Exporta el reporte de asistencias |
| GET | `/excel/plantilla-estudiantes` | Descarga plantilla de estudiantes |
| GET | `/excel/plantilla-fechas` | Descarga plantilla de fechas |

## Reglas de negocio

- **Un solo módulo activo:** activar un módulo desactiva los anteriores.
- **Modificación auditada:** cambiar PRESENTE/FALTA exige un motivo; queda registrado en `AuditoriaAsistencia` de forma inmutable.
- **Sincronización automática:** al consultar la matriz se generan registros `FALTA` para fechas de clase ya ocurridas sin marcaje.
- **Reinicio de módulo:** desactiva el módulo activo (sin borrar datos) y deja registro en la bitácora con el motivo.

## Cuentas de demostración

`DataInitializer` siembra datos de prueba al iniciar con una base de datos vacía:

| Rol | Credencial |
|---|---|
| Administrador | `admin` / `admin123` (también `coordinador` / `admin123`) |
| Estudiantes | `EST-1001` … `EST-1005` (o los correos `*.@diplomado.edu`) |

> ⚠️ **Solo para desarrollo local.** En producción: usuarios reales en base de datos, contraseñas hasheadas y credenciales fuera del código fuente.

## Notas

- El schema se gestiona con `spring.jpa.hibernate.ddl-auto=update` (no hay Flyway/Liquibase).
- CORS está configurado para el frontend Vite en `http://localhost:5173` (`config/CorsConfig.java`).
- La zona horaria de la aplicación es `America/La_Paz`.
- No hay tests unitarios ni de integración todavía (`src/test` no existe).
