const API_BASE = '/api';

async function request(endpoint, options = {}) {
  const url = `${API_BASE}${endpoint}`;
  const config = {
    ...options,
    headers: {
      ...(options.headers || {}),
    },
  };

  // If body is not FormData, default to JSON content-type
  if (options.body && !(options.body instanceof FormData) && !config.headers['Content-Type']) {
    config.headers['Content-Type'] = 'application/json';
  }

  try {
    const res = await fetch(url, config);
    
    // For blob downloads (Excel export)
    if (options.responseType === 'blob') {
      if (!res.ok) {
        throw new Error('Error al descargar el archivo');
      }
      return await res.blob();
    }

    const json = await res.json();
    if (!res.ok || json.success === false) {
      throw new Error(json.message || 'Error en la petición al servidor');
    }
    return json;
  } catch (error) {
    console.error(`API Error [${endpoint}]:`, error);
    throw error;
  }
}

export const api = {
  // Autenticación
  loginEstudiante: (identificador) => 
    request('/auth/estudiante/login', {
      method: 'POST',
      body: JSON.stringify({ identificador })
    }),

  loginAdmin: (username, password) => 
    request('/auth/admin/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    }),

  // Portal Estudiante
  getEstudianteResumen: (estudianteId) => 
    request(`/estudiante/${estudianteId}/resumen`),

  marcarAsistencia: (estudianteId) => 
    request('/estudiante/marcar', {
      method: 'POST',
      body: JSON.stringify({ estudianteId })
    }),

  // Gestión de Módulos
  getModulos: () => request('/admin/modulos'),
  getModuloActivo: () => request('/admin/modulos/activo'),
  activarNuevoModulo: (nombre, codigo, usuario = 'Administrador') =>
    request(`/admin/modulos/activar-nuevo?usuario=${encodeURIComponent(usuario)}`, {
      method: 'POST',
      body: JSON.stringify({ nombre, codigo })
    }),
  activarModuloExistente: (id, usuario = 'Administrador') =>
    request(`/admin/modulos/${id}/activar?usuario=${encodeURIComponent(usuario)}`, {
      method: 'POST'
    }),
  reiniciarModulo: (motivo, usuario = 'Administrador') =>
    request(`/admin/modulos/reiniciar?usuario=${encodeURIComponent(usuario)}`, {
      method: 'POST',
      body: JSON.stringify({ motivo })
    }),
  getDashboardStats: (moduloId) => 
    request(`/admin/modulos/${moduloId}/dashboard`),

  // Estudiantes
  getEstudiantes: (moduloId, q = '') => 
    request(`/admin/modulos/${moduloId}/estudiantes${q ? `?q=${encodeURIComponent(q)}` : ''}`),
  crearEstudiante: (moduloId, data) =>
    request(`/admin/modulos/${moduloId}/estudiantes`, {
      method: 'POST',
      body: JSON.stringify(data)
    }),

  // Fechas de Clase
  getFechas: (moduloId) => 
    request(`/admin/modulos/${moduloId}/fechas`),
  crearFecha: (moduloId, data) =>
    request(`/admin/modulos/${moduloId}/fechas`, {
      method: 'POST',
      body: JSON.stringify(data)
    }),

  // Matriz de Asistencias
  getMatriz: (moduloId, fechaId = null, q = '') => {
    const params = new URLSearchParams();
    if (fechaId) params.append('fechaId', fechaId);
    if (q) params.append('q', q);
    const queryString = params.toString() ? `?${params.toString()}` : '';
    return request(`/admin/modulos/${moduloId}/matriz${queryString}`);
  },

  // Modificación auditada (RN-06, RN-07)
  modificarAsistencia: (asistenciaId, nuevoEstado, motivo, usuarioModificacion = 'Administrador') =>
    request('/admin/asistencias/modificar', {
      method: 'PUT',
      body: JSON.stringify({ asistenciaId, nuevoEstado, motivo, usuarioModificacion })
    }),

  sincronizarAsistencias: (moduloId) =>
    request(`/admin/modulos/${moduloId}/sincronizar`, { method: 'POST' }),

  // Cargas y Previews de Excel (RF-11, RF-12, RF-16)
  previewExcelEstudiantes: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return request('/admin/excel/preview-estudiantes', {
      method: 'POST',
      body: formData
    });
  },

  previewExcelFechas: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return request('/admin/excel/preview-fechas', {
      method: 'POST',
      body: formData
    });
  },

  importarExcelEstudiantes: (moduloId, file, usuario = 'Administrador') => {
    const formData = new FormData();
    formData.append('file', file);
    return request(`/admin/modulos/${moduloId}/importar-estudiantes?usuario=${encodeURIComponent(usuario)}`, {
      method: 'POST',
      body: formData
    });
  },

  importarExcelFechas: (moduloId, file, usuario = 'Administrador') => {
    const formData = new FormData();
    formData.append('file', file);
    return request(`/admin/modulos/${moduloId}/importar-fechas?usuario=${encodeURIComponent(usuario)}`, {
      method: 'POST',
      body: formData
    });
  },

  // Exportación a Excel (RF-17)
  exportarExcel: async (moduloId, nombreModulo = 'Modulo') => {
    const blob = await request(`/admin/modulos/${moduloId}/exportar-excel`, {
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Asistencias_${nombreModulo.replace(/[^a-zA-Z0-9.-]/g, '_')}.xlsx`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  },

  // Auditorías y Actividades
  getAuditorias: (moduloId) => 
    request(`/admin/modulos/${moduloId}/auditorias`),
  getActividades: (moduloId) => 
    request(`/admin/modulos/${moduloId}/actividades`),
  getTodasActividades: () => 
    request('/admin/actividades'),

  // Descarga de Plantillas Excel
  descargarPlantillaEstudiantes: async () => {
    const blob = await request('/admin/excel/plantilla-estudiantes', {
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'Plantilla_Estudiantes.xlsx';
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  },

  descargarPlantillaFechas: async () => {
    const blob = await request('/admin/excel/plantilla-fechas', {
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'Plantilla_Fechas.xlsx';
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  }
};

