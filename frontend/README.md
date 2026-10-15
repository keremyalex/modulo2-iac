# Frontend — Sistema de Control y Registro de Asistencia Estudiantil

Interfaz web (SPA) para la gestión de asistencia de estudiantes de un diplomado.

**Stack:** React 19 · Vite 8 · Tailwind CSS 4 · Lucide Icons · oxlint

---

## Requisitos

- Node.js 18+ y npm
- Backend corriendo en `http://localhost:8080` (ver `backend/README.md`)

## Puesta en marcha

```bash
# Desde la carpeta frontend/
npm install
npm run dev
```

La aplicación queda disponible en `http://localhost:5173`.

## Scripts

| Comando | Descripción |
|---|---|
| `npm run dev` | Servidor de desarrollo con HMR |
| `npm run build` | Build de producción en `dist/` |
| `npm run preview` | Vista previa del build de producción |
| `npm run lint` | Análisis estático con oxlint |

## Configuración

- **Proxy de desarrollo:** Vite redirige `/api` → `http://localhost:8080` (`vite.config.js`), así que en desarrollo no hay que configurar la URL del backend.
- **Cliente API:** `src/services/api.js` (`API_BASE = '/api'`); en producción debe resolverse mediante un reverse proxy que mapee `/api` al backend.

## Estructura

```
frontend/
├── index.html
├── vite.config.js
├── public/                     # Plantillas Excel descargables
└── src/
    ├── main.jsx                # Punto de entrada React
    ├── App.jsx                 # Sesión (localStorage), routing por rol, toasts
    ├── index.css               # Tailwind 4 + estilos base
    ├── services/
    │   └── api.js              # Cliente fetch con todos los endpoints
    └── components/
        ├── LoginView.jsx           # Login dual (estudiante / administrador)
        ├── EstudianteView.jsx      # Portal del estudiante
        ├── AdminView.jsx           # Panel de administración (5 tabs)
        ├── Navbar.jsx
        ├── ModalModificarAsistencia.jsx
        ├── ModalNuevoModulo.jsx
        └── ModalReinicioModulo.jsx
```

## Vistas

**Login dual (`LoginView.jsx`)** — pestañas para seleccionar Portal Estudiante o Administración.

**Portal del Estudiante (`EstudianteView.jsx`)**
- Saludo personalizado, N° de registro, correo y módulo activo
- Tarjeta de la clase de hoy con botón interactivo para marcaje en tiempo real
- Conteo de presentes/faltas y porcentaje de asistencia
- Historial por sesión con hora exacta de registro

**Panel del Administrador (`AdminView.jsx`)** — 5 pestañas:
- **Matriz de Asistencia:** cuadrícula estudiantes × fechas, filtros en tiempo real y celdas editables con modificación auditada (motivo obligatorio)
- **Estudiantes:** lista con búsqueda y alta manual
- **Carga Excel:** previsualización de importaciones, reporte de errores por fila y descarga de plantillas
- **Auditoría y Actividades:** historial de intervenciones administrativas y bitácora del sistema
- **Módulos:** histórico de módulos y activación de nuevos ciclos

## Cuentas de demostración

Los datos de prueba los siembra el backend (`DataInitializer`); el login ofrece botones de un clic:

| Rol | Credencial |
|---|---|
| Administrador | `admin` / `admin123` |
| Estudiantes | `EST-1001` (Carlos Pérez), `EST-1002` (Mariana Morales) |

> ⚠️ **Solo para desarrollo local.** En producción: credenciales reales y autenticación del lado del servidor.

## Notas

- **Sin enrutador:** la vista se elige según `user.rol` (`ADMIN` / `ESTUDIANTE`) en `App.jsx`; la sesión se guarda en `localStorage` bajo la clave `asistencia_user`.
- El backend aún no valida tokens en los endpoints: la protección real debe resolverse del lado servidor (Spring Security).
- Lint: [oxlint](https://oxc.rs) con las reglas de `frontend/.oxlintrc.json` (hooks de React en error).
- `AdminView.jsx` concentra la mayoría de la lógica del panel (~1.168 líneas); es candidato a extracción por tabs si el proyecto crece.
