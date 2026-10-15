package com.diplomado.asistencia.controller;

import com.diplomado.asistencia.dto.ApiResponse;
import com.diplomado.asistencia.dto.LoginAdminRequest;
import com.diplomado.asistencia.dto.LoginEstudianteRequest;
import com.diplomado.asistencia.dto.LoginResponse;
import com.diplomado.asistencia.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/estudiante/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginEstudiante(@Valid @RequestBody LoginEstudianteRequest request) {
        LoginResponse response = authService.loginEstudiante(request);
        return ResponseEntity.ok(ApiResponse.ok("Acceso concedido exitosamente al módulo activo", response));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginAdmin(@Valid @RequestBody LoginAdminRequest request) {
        LoginResponse response = authService.loginAdmin(request);
        return ResponseEntity.ok(ApiResponse.ok("Autenticación administrativa exitosa", response));
    }
}
