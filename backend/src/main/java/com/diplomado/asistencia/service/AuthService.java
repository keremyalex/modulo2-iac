package com.diplomado.asistencia.service;

import com.diplomado.asistencia.dto.LoginAdminRequest;
import com.diplomado.asistencia.dto.LoginEstudianteRequest;
import com.diplomado.asistencia.dto.LoginResponse;
import com.diplomado.asistencia.entity.Estudiante;
import com.diplomado.asistencia.entity.Modulo;
import com.diplomado.asistencia.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final EstudianteService estudianteService;
    private final ModuloService moduloService;
    private final ActividadService actividadService;

    public AuthService(EstudianteService estudianteService, ModuloService moduloService, ActividadService actividadService) {
        this.estudianteService = estudianteService;
        this.moduloService = moduloService;
        this.actividadService = actividadService;
    }

    public LoginResponse loginEstudiante(LoginEstudianteRequest request) {
        Modulo moduloActivo = moduloService.getModuloActivo();

        Estudiante estudiante = estudianteService.buscarPorIdentificadorEnModuloActivo(request.getIdentificador());

        String token = "TOKEN_EST_" + UUID.randomUUID().toString();

        actividadService.registrar(
                estudiante.getNombreCompleto(),
                "LOGIN_ESTUDIANTE",
                "Ingreso al portal del estudiante " + estudiante.getNombreCompleto() + " (" + estudiante.getNumeroRegistro() + ")",
                moduloActivo
        );

        return new LoginResponse(
                token,
                "ESTUDIANTE",
                estudiante.getNumeroRegistro(),
                estudiante.getNombreCompleto(),
                estudiante.getId(),
                moduloActivo.getId(),
                moduloActivo.getNombre()
        );
    }

    public LoginResponse loginAdmin(LoginAdminRequest request) {
        String u = request.getUsername().trim();
        String p = request.getPassword().trim();

        // Credenciales administrativas del sistema
        if (("admin".equalsIgnoreCase(u) && "admin123".equals(p)) ||
            ("coordinador".equalsIgnoreCase(u) && "admin123".equals(p))) {

            String token = "TOKEN_ADM_" + UUID.randomUUID().toString();
            Optional<Modulo> moduloActivo = moduloService.getModuloActivoOptional();

            Long modId = moduloActivo.map(Modulo::getId).orElse(null);
            String modNombre = moduloActivo.map(Modulo::getNombre).orElse("Sin módulo activo");

            actividadService.registrar(
                    u,
                    "LOGIN_ADMINISTRADOR",
                    "Inicio de sesión exitoso del administrador: " + u,
                    moduloActivo.orElse(null)
            );

            return new LoginResponse(
                    token,
                    "ADMIN",
                    u,
                    "Administrador Académico",
                    null,
                    modId,
                    modNombre
            );
        }

        throw new BusinessException("Credenciales administrativas inválidas. Verifique su usuario y contraseña.");
    }
}
