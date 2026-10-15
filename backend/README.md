Sistema de Control y Registro de Asistencia Estudiantil
Se ha desarrollado e integrado de principio a fin la Aplicación Web de Control y Registro de Asistencia Estudiantil, estructurada en una arquitectura desacoplada y robusta:

Backend: 

backA
(Java 17, Spring Boot 3.3.4, Spring Data JPA, PostgreSQL, Apache POI).
Frontend: 

FrontA
(React 19, Vite, Tailwind CSS, Lucide Icons).
Base de Datos: PostgreSQL
-----------------------------------------------------------------------------------------------------------------
2. Interfaz de Usuario Frontend (

FrontA
)
Página de Inicio Dual (

LoginView.jsx
): Pestañas para Portal Estudiante (con botones de un clic para usuarios de prueba como EST-1001) y Administrador (admin / admin123).
Portal del Estudiante (

EstudianteView.jsx
):
Saludo personalizado, N° de registro, correo y módulo activo.
Tarjeta de la Clase de Hoy con horario programado y botón interactivo grande para marcaje en tiempo real.
Conteo de presentes, faltas y medidor de porcentaje de asistencia.
Tabla con historial por sesión, estados visuales y hora exacta de registro.
Panel de Control del Administrador (

AdminView.jsx
):
Tarjetas KPI: Totales de Estudiantes, Sesiones, Asistencias, Tasa Promedio y Auditorías registradas.
Matriz de Asistencia: Visualización en cuadrícula con estudiantes en filas y fechas en columnas, filtros en tiempo real y celdas interactivas.
Modal de Modificación Auditada (

ModalModificarAsistencia.jsx
): Permite conmutar PRESENTE o FALTA exigiendo el motivo obligatorio para la auditoría inmutable.
Carga de Archivos Excel: Subida con previsualización (RF-16), reporte de errores de fila y botones para descargar plantillas de muestra listas para usar.
Exportación Excel: Descarga con un clic del reporte oficial formateado con Apache POI (RF-17).
Auditoría & Actividades: Historial completo de intervenciones administrativas y bitácora del sistema.
Modal de Reinicio de Módulo (

ModalReinicioModulo.jsx
): Confirmación con palabra clave REINICIAR y justificación (RN-08).
----------------------------------------------------------------------------------------
3. Instrucciones de Puesta en Marcha
Ambos servidores ya se encuentran configurados y en ejecución en segundo plano:

Servidor Backend (Spring Boot)
Directorio: c:\Users\User\Desktop\backA
Comando para iniciar manualmente:
powershell
cd c:\Users\User\Desktop\backA
mvn spring-boot:run
Puerto: http://localhost:8080
Servidor Frontend (React + Vite)
Directorio: c:\Users\User\Desktop\FrontA
Comando para iniciar manualmente:
powershell
cd c:\Users\User\Desktop\FrontA
npm run dev
Puerto: http://localhost:5173
--------------------------------------------------------------------------------------------
Credenciales de Estudiante:
Estudiante 1: EST-1001 (o correo carlos.perez@diplomado.edu)
Estudiante 2: EST-1002 (o correo mariana.morales@diplomado.edu)
O LO QUE ESTE EN LA BASE DE DATOS
Credenciales de Administrador:
Usuario: admin
Contraseña: admin123
