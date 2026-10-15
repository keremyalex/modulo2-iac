import React, { useState, useEffect } from 'react';
import {
  Users, Calendar, CheckCircle2, XCircle, FileSpreadsheet, ShieldAlert,
  Search, Upload, Download, Plus, RefreshCw, Filter, AlertTriangle,
  History, Clock, Check, Eye, BookOpen, Layers, ArrowRight
} from 'lucide-react';
import { api } from '../services/api';
import ModalModificarAsistencia from './ModalModificarAsistencia';
import ModalNuevoModulo from './ModalNuevoModulo';
import ModalReinicioModulo from './ModalReinicioModulo';

export default function AdminView({ user, moduloActivo, onRefreshModulo, onNotification }) {
  const [activeTab, setActiveTab] = useState('MATRIZ'); // 'MATRIZ' | 'ESTUDIANTES' | 'CARGA_EXCEL' | 'AUDITORIA' | 'MODULOS'
  const [loading, setLoading] = useState(false);

  // Datos principales
  const [stats, setStats] = useState(null);
  const [matriz, setMatriz] = useState(null);
  const [filtroFechaId, setFiltroFechaId] = useState('');
  const [filtroEstudiante, setFiltroEstudiante] = useState('');
  const [fechasList, setFechasList] = useState([]);
  const [estudiantesList, setEstudiantesList] = useState([]);
  const [auditoriasList, setAuditoriasList] = useState([]);
  const [actividadesList, setActividadesList] = useState([]);
  const [todosModulos, setTodosModulos] = useState([]);

  // Modales
  const [selectedCelda, setSelectedCelda] = useState(null);
  const [showNuevoModuloModal, setShowNuevoModuloModal] = useState(false);
  const [showReinicioModal, setShowReinicioModal] = useState(false);

  // Estados de Carga Excel
  const [tipoExcel, setTipoExcel] = useState('ESTUDIANTES'); // 'ESTUDIANTES' | 'FECHAS'
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewData, setPreviewData] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [importLoading, setImportLoading] = useState(false);

  // Formulario manual de estudiante
  const [showNuevoEstudianteForm, setShowNuevoEstudianteForm] = useState(false);
  const [nuevoReg, setNuevoReg] = useState('');
  const [nuevoNom, setNuevoNom] = useState('');
  const [nuevoCor, setNuevoCor] = useState('');
  const [formEstError, setFormEstError] = useState('');

  // Formulario manual de fecha
  const [showNuevaFechaForm, setShowNuevaFechaForm] = useState(false);
  const [nuevaFecha, setNuevaFecha] = useState('');
  const [nuevaHoraIni, setNuevaHoraIni] = useState('08:30');
  const [nuevaHoraFin, setNuevaHoraFin] = useState('12:30');
  const [formFechaError, setFormFechaError] = useState('');

  // Carga inicial y por módulo
  useEffect(() => {
    if (moduloActivo?.id) {
      cargarDatosModulo();
    }
    cargarModulosHistoricos();
  }, [moduloActivo]);

  const cargarDatosModulo = async () => {
    if (!moduloActivo?.id) return;
    setLoading(true);
    try {
      const [resStats, resFechas, resMatriz] = await Promise.all([
        api.getDashboardStats(moduloActivo.id),
        api.getFechas(moduloActivo.id),
        api.getMatriz(moduloActivo.id, filtroFechaId || null, filtroEstudiante || '')
      ]);
      setStats(resStats.data);
      setFechasList(resFechas.data || []);
      setMatriz(resMatriz.data);
    } catch (err) {
      if (onNotification) onNotification('error', err.message);
    } finally {
      setLoading(false);
    }
  };

  const cargarMatriz = async () => {
    if (!moduloActivo?.id) return;
    try {
      const resMatriz = await api.getMatriz(moduloActivo.id, filtroFechaId || null, filtroEstudiante || '');
      setMatriz(resMatriz.data);
    } catch (err) {
      if (onNotification) onNotification('error', err.message);
    }
  };

  const cargarEstudiantes = async () => {
    if (!moduloActivo?.id) return;
    try {
      const res = await api.getEstudiantes(moduloActivo.id, filtroEstudiante);
      setEstudiantesList(res.data || []);
    } catch (err) {
      if (onNotification) onNotification('error', err.message);
    }
  };

  const cargarAuditoriasYActividades = async () => {
    if (!moduloActivo?.id) return;
    try {
      const [resAud, resAct] = await Promise.all([
        api.getAuditorias(moduloActivo.id),
        api.getActividades(moduloActivo.id)
      ]);
      setAuditoriasList(resAud.data || []);
      setActividadesList(resAct.data || []);
    } catch (err) {
      if (onNotification) onNotification('error', err.message);
    }
  };

  const cargarModulosHistoricos = async () => {
    try {
      const res = await api.getModulos();
      setTodosModulos(res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  // Filtrado de matriz al cambiar filtros
  useEffect(() => {
    if (moduloActivo?.id && activeTab === 'MATRIZ') {
      const timer = setTimeout(() => {
        cargarMatriz();
      }, 300);
      return () => clearTimeout(timer);
    }
  }, [filtroFechaId, filtroEstudiante]);

  // Cambio de pestañas
  useEffect(() => {
    if (activeTab === 'ESTUDIANTES') cargarEstudiantes();
    if (activeTab === 'AUDITORIA') cargarAuditoriasYActividades();
    if (activeTab === 'MODULOS') cargarModulosHistoricos();
    if (activeTab === 'MATRIZ') cargarMatriz();
  }, [activeTab]);

  // Manejo de Previsualización Excel (RF-16)
  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setSelectedFile(file);
    setPreviewLoading(true);
    setPreviewData(null);

    try {
      let res;
      if (tipoExcel === 'ESTUDIANTES') {
        res = await api.previewExcelEstudiantes(file);
      } else {
        res = await api.previewExcelFechas(file);
      }
      setPreviewData(res.data);
      if (onNotification) onNotification('success', `Vista previa de ${res.data.totalFilas} filas procesada.`);
    } catch (err) {
      if (onNotification) onNotification('error', err.message);
      setSelectedFile(null);
    } finally {
      setPreviewLoading(false);
    }
  };

  // Confirmar Importación de Excel (RF-11, RF-12)
  const handleConfirmarImportacion = async () => {
    if (!selectedFile || !moduloActivo?.id) return;
    setImportLoading(true);
    try {
      if (tipoExcel === 'ESTUDIANTES') {
        const res = await api.importarExcelEstudiantes(moduloActivo.id, selectedFile, user?.nombre || 'Administrador');
        if (onNotification) onNotification('success', res.message || 'Estudiantes importados correctamente.');
      } else {
        const res = await api.importarExcelFechas(moduloActivo.id, selectedFile, user?.nombre || 'Administrador');
        if (onNotification) onNotification('success', res.message || 'Fechas importadas correctamente.');
      }
      setSelectedFile(null);
      setPreviewData(null);
      cargarDatosModulo();
      setActiveTab('MATRIZ');
    } catch (err) {
      if (onNotification) onNotification('error', err.message);
    } finally {
      setImportLoading(false);
    }
  };

  // Exportar Excel (RF-17)
  const handleExportarExcel = async () => {
    if (!moduloActivo?.id) return;
    try {
      if (onNotification) onNotification('info', 'Generando reporte Excel con Apache POI...');
      await api.exportarExcel(moduloActivo.id, moduloActivo.nombre);
      if (onNotification) onNotification('success', 'Reporte Excel descargado exitosamente.');
    } catch (err) {
      if (onNotification) onNotification('error', err.message);
    }
  };

  // Registrar Estudiante Manual
  const handleCrearEstudiante = async (e) => {
    e.preventDefault();
    if (!nuevoReg.trim() || !nuevoNom.trim() || !nuevoCor.trim()) {
      setFormEstError('Todos los campos son obligatorios.');
      return;
    }
    setFormEstError('');
    try {
      await api.crearEstudiante(moduloActivo.id, {
        numeroRegistro: nuevoReg.trim(),
        nombreCompleto: nuevoNom.trim(),
        correo: nuevoCor.trim().toLowerCase()
      });
      if (onNotification) onNotification('success', 'Estudiante registrado correctamente.');
      setNuevoReg('');
      setNuevoNom('');
      setNuevoCor('');
      setShowNuevoEstudianteForm(false);
      cargarEstudiantes();
      cargarMatriz();
    } catch (err) {
      setFormEstError(err.message || 'Error al registrar estudiante');
    }
  };

  // Registrar Fecha Manual
  const handleCrearFecha = async (e) => {
    e.preventDefault();
    if (!nuevaFecha || !nuevaHoraIni || !nuevaHoraFin) {
      setFormFechaError('Todos los campos de fecha y horario son obligatorios.');
      return;
    }
    setFormFechaError('');
    try {
      await api.crearFecha(moduloActivo.id, {
        fecha: nuevaFecha,
        horaInicio: nuevaHoraIni,
        horaFin: nuevaHoraFin
      });
      if (onNotification) onNotification('success', 'Fecha de clase programada exitosamente.');
      setNuevaFecha('');
      setShowNuevaFechaForm(false);
      cargarDatosModulo();
    } catch (err) {
      setFormFechaError(err.message || 'Error al programar fecha');
    }
  };

  // Cambiar Módulo Activo
  const handleActivarModuloExistente = async (id) => {
    try {
      await api.activarModuloExistente(id, user?.nombre || 'Administrador');
      if (onNotification) onNotification('success', 'Módulo activado exitosamente.');
      onRefreshModulo();
      cargarModulosHistoricos();
    } catch (err) {
      if (onNotification) onNotification('error', err.message);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Banner & Module Info */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-black text-slate-900 tracking-tight">Panel Administrativo</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-100 text-indigo-700">
              Gestión Integral
            </span>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Módulo actual: <strong className="text-slate-800">{moduloActivo ? moduloActivo.nombre : 'Sin módulo activo'}</strong>
          </p>
        </div>

        {/* Global Action Buttons */}
        <div className="flex flex-wrap items-center gap-2.5">
          <button
            onClick={() => setShowNuevoModuloModal(true)}
            className="flex items-center gap-2 px-3.5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold rounded-xl shadow-xs transition-colors cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            <span>Activar Nuevo Módulo</span>
          </button>

          {moduloActivo && (
            <>
              <button
                onClick={handleExportarExcel}
                className="flex items-center gap-2 px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-xl shadow-xs transition-colors cursor-pointer"
                title="Exportar asistencias a Excel (RF-17)"
              >
                <Download className="w-4 h-4" />
                <span>Exportar Excel</span>
              </button>

              <button
                onClick={() => setShowReinicioModal(true)}
                className="flex items-center gap-2 px-3.5 py-2 bg-amber-50 hover:bg-amber-100 text-amber-800 border border-amber-200 text-xs font-bold rounded-xl transition-colors cursor-pointer"
                title="Reiniciar ciclo operativo del módulo (RN-08)"
              >
                <RefreshCw className="w-4 h-4" />
                <span>Reiniciar Módulo</span>
              </button>
            </>
          )}
        </div>
      </div>

      {/* KPI Stats Cards */}
      {stats && (
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between text-slate-400 mb-2">
              <span className="text-xs font-semibold uppercase text-slate-500">Estudiantes</span>
              <Users className="w-4 h-4 text-blue-600" />
            </div>
            <div className="text-2xl font-black text-slate-900">{stats.totalEstudiantes}</div>
            <div className="text-[11px] text-slate-500 mt-1">Inscritos en el módulo</div>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between text-slate-400 mb-2">
              <span className="text-xs font-semibold uppercase text-slate-500">Sesiones</span>
              <Calendar className="w-4 h-4 text-indigo-600" />
            </div>
            <div className="text-2xl font-black text-slate-900">{stats.totalFechas}</div>
            <div className="text-[11px] text-slate-500 mt-1">Fechas planificadas</div>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between text-slate-400 mb-2">
              <span className="text-xs font-semibold uppercase text-emerald-600">Asistencias</span>
              <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            </div>
            <div className="text-2xl font-black text-emerald-600">{stats.totalPresentes}</div>
            <div className="text-[11px] text-emerald-700/70 mt-1">Marcajes presentes</div>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between text-slate-400 mb-2">
              <span className="text-xs font-semibold uppercase text-slate-500">Promedio</span>
              <Layers className="w-4 h-4 text-amber-600" />
            </div>
            <div className="text-2xl font-black text-slate-900">{stats.tasaAsistenciaPromedio}%</div>
            <div className="text-[11px] text-slate-500 mt-1">Tasa de presencia global</div>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs col-span-2 sm:col-span-1">
            <div className="flex items-center justify-between text-slate-400 mb-2">
              <span className="text-xs font-semibold uppercase text-indigo-600">Auditorías</span>
              <ShieldAlert className="w-4 h-4 text-indigo-600" />
            </div>
            <div className="text-2xl font-black text-indigo-600">{stats.totalAuditorias}</div>
            <div className="text-[11px] text-indigo-700/70 mt-1">Ajustes registrados</div>
          </div>
        </div>
      )}

      {/* Navigation Tabs */}
      <div className="border-b border-slate-200">
        <nav className="flex space-x-2 sm:space-x-8 overflow-x-auto pb-px">
          {[
            { id: 'MATRIZ', label: 'Matriz de Asistencias', icon: Layers },
            { id: 'ESTUDIANTES', label: 'Lista de Estudiantes', icon: Users },
            { id: 'CARGA_EXCEL', label: 'Carga Excel (RF-11/12/16)', icon: FileSpreadsheet },
            { id: 'AUDITORIA', label: 'Auditoría & Actividades (RN-06/07)', icon: History },
            { id: 'MODULOS', label: 'Módulos Históricos (RN-01)', icon: BookOpen },
          ].map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center gap-2 py-3 px-3 sm:px-1 text-xs sm:text-sm font-bold border-b-2 transition-colors whitespace-nowrap cursor-pointer ${
                  isActive
                    ? 'border-blue-600 text-blue-600'
                    : 'border-transparent text-slate-500 hover:text-slate-800 hover:border-slate-300'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </nav>
      </div>

      {/* TAB 1: MATRIZ DE ASISTENCIA */}
      {activeTab === 'MATRIZ' && (
        <div className="space-y-6">
          {/* Filters Bar */}
          <div className="bg-white p-4 sm:p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col md:flex-row items-center justify-between gap-4">
            <div className="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
              {/* Search input */}
              <div className="relative w-full sm:w-72">
                <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
                <input
                  type="text"
                  value={filtroEstudiante}
                  onChange={(e) => setFiltroEstudiante(e.target.value)}
                  placeholder="Buscar estudiante o registro..."
                  className="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-300 rounded-xl text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-900"
                />
              </div>

              {/* Date Filter Dropdown */}
              <div className="relative w-full sm:w-56">
                <select
                  value={filtroFechaId}
                  onChange={(e) => setFiltroFechaId(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-900"
                >
                  <option value="">Todas las fechas ({fechasList.length})</option>
                  {fechasList.map((f) => (
                    <option key={f.id} value={f.id}>
                      {f.fecha} ({f.horaInicio.slice(0, 5)})
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Quick Helper Actions */}
            <div className="flex items-center gap-2 self-end md:self-center">
              <button
                onClick={() => setShowNuevaFechaForm(!showNuevaFechaForm)}
                className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-xl transition-colors cursor-pointer"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Agregar Fecha Manual</span>
              </button>

              <button
                onClick={cargarMatriz}
                disabled={loading}
                title="Actualizar matriz"
                className="p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-xl transition-colors cursor-pointer"
              >
                <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
              </button>
            </div>
          </div>

          {/* Formulario Inline para agregar fecha de clase */}
          {showNuevaFechaForm && (
            <div className="p-5 bg-blue-50/60 rounded-2xl border border-blue-200 animate-fadeIn">
              <h3 className="text-xs font-bold uppercase tracking-wider text-blue-900 mb-3">
                Programar Nueva Fecha de Clase
              </h3>
              {formFechaError && (
                <div className="p-2.5 bg-rose-50 border border-rose-200 text-xs text-rose-700 rounded-lg mb-3">
                  {formFechaError}
                </div>
              )}
              <form onSubmit={handleCrearFecha} className="flex flex-wrap items-end gap-3">
                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 mb-1">Fecha</label>
                  <input
                    type="date"
                    value={nuevaFecha}
                    onChange={(e) => setNuevaFecha(e.target.value)}
                    className="px-3 py-1.5 bg-white border border-slate-300 rounded-lg text-xs"
                    required
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 mb-1">Hora Inicio</label>
                  <input
                    type="time"
                    value={nuevaHoraIni}
                    onChange={(e) => setNuevaHoraIni(e.target.value)}
                    className="px-3 py-1.5 bg-white border border-slate-300 rounded-lg text-xs"
                    required
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 mb-1">Hora Fin</label>
                  <input
                    type="time"
                    value={nuevaHoraFin}
                    onChange={(e) => setNuevaHoraFin(e.target.value)}
                    className="px-3 py-1.5 bg-white border border-slate-300 rounded-lg text-xs"
                    required
                  />
                </div>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold rounded-lg cursor-pointer"
                >
                  Guardar Fecha
                </button>
                <button
                  type="button"
                  onClick={() => setShowNuevaFechaForm(false)}
                  className="px-3 py-2 text-slate-600 hover:text-slate-800 text-xs font-semibold cursor-pointer"
                >
                  Cancelar
                </button>
              </form>
            </div>
          )}

          {/* Matrix Grid */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-200 bg-slate-50/70 flex items-center justify-between">
              <div>
                <h2 className="text-sm font-bold text-slate-900">Matriz Oficial de Asistencias</h2>
                <p className="text-xs text-slate-500">Haz clic en cualquier celda de estado para modificarla con auditoría (RN-06)</p>
              </div>
              <div className="flex items-center gap-3 text-xs">
                <span className="flex items-center gap-1.5 text-emerald-700 font-semibold">
                  <span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span> Presente
                </span>
                <span className="flex items-center gap-1.5 text-rose-700 font-semibold">
                  <span className="w-2.5 h-2.5 rounded-full bg-rose-500"></span> Falta
                </span>
              </div>
            </div>

            <div className="overflow-x-auto max-h-[600px] relative">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-100 text-slate-700 font-bold uppercase tracking-wider sticky top-0 z-20 shadow-xs border-b border-slate-200">
                  <tr>
                    <th className="px-4 py-3 bg-slate-100 sticky left-0 z-30 shadow-xs">N°</th>
                    <th className="px-4 py-3 bg-slate-100 sticky left-8 z-30 shadow-xs min-w-[200px]">Estudiante</th>
                    <th className="px-4 py-3 min-w-[100px]">Registro</th>

                    {/* Columnas de fechas */}
                    {matriz?.fechas?.map((f) => (
                      <th key={f.id} className="px-4 py-3 text-center min-w-[110px] bg-slate-100">
                        <div className="font-bold text-slate-800">{f.fecha}</div>
                        <div className="text-[10px] text-slate-500 lowercase font-normal">{f.horaInicio.slice(0, 5)} - {f.horaFin.slice(0, 5)}</div>
                      </th>
                    ))}

                    <th className="px-4 py-3 text-center min-w-[80px]">Pres.</th>
                    <th className="px-4 py-3 text-center min-w-[80px]">Faltas</th>
                    <th className="px-4 py-3 text-center min-w-[90px]">% Asist.</th>
                  </tr>
                </thead>

                <tbody className="divide-y divide-slate-100">
                  {matriz?.estudiantes?.map((est, idx) => (
                    <tr key={est.estudianteId} className="hover:bg-slate-50 transition-colors">
                      <td className="px-4 py-3 font-semibold text-slate-400 sticky left-0 bg-white z-10">
                        {idx + 1}
                      </td>
                      <td className="px-4 py-3 font-bold text-slate-800 sticky left-8 bg-white z-10 truncate max-w-[220px]">
                        <div>{est.nombreCompleto}</div>
                        <div className="text-[10px] font-normal text-slate-400">{est.correo}</div>
                      </td>
                      <td className="px-4 py-3 font-mono text-slate-600">
                        {est.numeroRegistro}
                      </td>

                      {/* Celdas por fecha */}
                      {matriz.fechas.map((f) => {
                        const celda = est.asistenciasPorFecha[f.id];
                        const isPresente = celda?.estado === 'PRESENTE';

                        return (
                          <td key={f.id} className="px-3 py-2.5 text-center">
                            {celda ? (
                              <button
                                onClick={() => setSelectedCelda({ celdaData: celda, estudiante: est, fecha: f })}
                                title={`Clic para modificar asistencia (${celda.estado})`}
                                className={`w-full py-1.5 px-2 rounded-lg font-bold text-[11px] transition-all flex flex-col items-center justify-center gap-0.5 cursor-pointer hover:ring-2 ${
                                  isPresente
                                    ? 'bg-emerald-100 text-emerald-800 hover:bg-emerald-200 ring-emerald-400'
                                    : 'bg-rose-100 text-rose-800 hover:bg-rose-200 ring-rose-400'
                                }`}
                              >
                                <span className="flex items-center gap-1">
                                  {isPresente ? <Check className="w-3 h-3 text-emerald-700" /> : <span className="text-[10px]">✕</span>}
                                  {celda.estado}
                                </span>
                                {celda.horaMarcaje && (
                                  <span className="text-[9px] font-normal text-emerald-700/80 font-mono">
                                    {celda.horaMarcaje.slice(0, 5)}
                                  </span>
                                )}
                              </button>
                            ) : (
                              <span className="text-slate-300">-</span>
                            )}
                          </td>
                        );
                      })}

                      <td className="px-4 py-3 text-center font-bold text-emerald-600">
                        {est.totalPresentes}
                      </td>
                      <td className="px-4 py-3 text-center font-bold text-rose-600">
                        {est.totalFaltas}
                      </td>
                      <td className="px-4 py-3 text-center">
                        <span className={`px-2 py-0.5 rounded-full font-bold text-[11px] ${
                          est.porcentajeAsistencia >= 80
                            ? 'bg-emerald-100 text-emerald-800'
                            : est.porcentajeAsistencia >= 60
                            ? 'bg-amber-100 text-amber-800'
                            : 'bg-rose-100 text-rose-800'
                        }`}>
                          {est.porcentajeAsistencia}%
                        </span>
                      </td>
                    </tr>
                  ))}

                  {(!matriz?.estudiantes || matriz.estudiantes.length === 0) && (
                    <tr>
                      <td colSpan={6 + (matriz?.fechas?.length || 0)} className="px-6 py-16 text-center text-slate-400">
                        No se encontraron estudiantes para los filtros seleccionados o el módulo está vacío.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: LISTA DE ESTUDIANTES */}
      {activeTab === 'ESTUDIANTES' && (
        <div className="space-y-6">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-white p-4 rounded-2xl border border-slate-200">
            <div className="relative w-full sm:w-80">
              <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
              <input
                type="text"
                value={filtroEstudiante}
                onChange={(e) => setFiltroEstudiante(e.target.value)}
                placeholder="Buscar por nombre, carnet o correo..."
                className="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-300 rounded-xl text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-900"
              />
            </div>

            <button
              onClick={() => setShowNuevoEstudianteForm(!showNuevoEstudianteForm)}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold rounded-xl cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Registrar Estudiante Individual</span>
            </button>
          </div>

          {/* Formulario Manual Nuevo Estudiante */}
          {showNuevoEstudianteForm && (
            <div className="p-5 bg-white rounded-2xl border border-slate-200 shadow-sm animate-fadeIn">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-800 mb-3">
                Nuevo Estudiante en Módulo Activo (RN-02)
              </h3>
              {formEstError && (
                <div className="p-2.5 bg-rose-50 border border-rose-200 text-xs text-rose-700 rounded-lg mb-3">
                  {formEstError}
                </div>
              )}
              <form onSubmit={handleCrearEstudiante} className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 mb-1">N° de Registro / Matrícula</label>
                  <input
                    type="text"
                    value={nuevoReg}
                    onChange={(e) => setNuevoReg(e.target.value)}
                    placeholder="Ej: EST-1006"
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                    required
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 mb-1">Nombre Completo</label>
                  <input
                    type="text"
                    value={nuevoNom}
                    onChange={(e) => setNuevoNom(e.target.value)}
                    placeholder="Ej: Laura Camila Suárez Ruiz"
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                    required
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 mb-1">Correo Electrónico</label>
                  <input
                    type="email"
                    value={nuevoCor}
                    onChange={(e) => setNuevoCor(e.target.value)}
                    placeholder="laura.suarez@diplomado.edu"
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                    required
                  />
                </div>
                <div className="sm:col-span-3 flex justify-end gap-2 pt-2">
                  <button
                    type="button"
                    onClick={() => setShowNuevoEstudianteForm(false)}
                    className="px-3 py-1.5 text-slate-600 text-xs font-semibold"
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 bg-blue-600 text-white text-xs font-bold rounded-lg cursor-pointer"
                  >
                    Registrar Estudiante
                  </button>
                </div>
              </form>
            </div>
          )}

          {/* Tabla de Estudiantes */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-100 text-slate-700 font-bold uppercase tracking-wider border-b border-slate-200">
                  <tr>
                    <th className="px-6 py-3.5">N°</th>
                    <th className="px-6 py-3.5">N° Registro</th>
                    <th className="px-6 py-3.5">Nombre Completo</th>
                    <th className="px-6 py-3.5">Correo Electrónico</th>
                    <th className="px-6 py-3.5">Módulo</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {estudiantesList.map((e, idx) => (
                    <tr key={e.id} className="hover:bg-slate-50 transition-colors">
                      <td className="px-6 py-3.5 font-semibold text-slate-400">{idx + 1}</td>
                      <td className="px-6 py-3.5 font-mono font-bold text-slate-800">{e.numeroRegistro}</td>
                      <td className="px-6 py-3.5 font-semibold text-slate-800">{e.nombreCompleto}</td>
                      <td className="px-6 py-3.5 text-slate-600">{e.correo}</td>
                      <td className="px-6 py-3.5 text-slate-500">{e.modulo?.nombre}</td>
                    </tr>
                  ))}
                  {estudiantesList.length === 0 && (
                    <tr>
                      <td colSpan="5" className="px-6 py-12 text-center text-slate-400">
                        No hay estudiantes inscritos en este módulo actualmente.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* TAB 3: CARGA EXCEL CON PREVISUALIZACIÓN (RF-11, RF-12, RF-16) */}
      {activeTab === 'CARGA_EXCEL' && (
        <div className="space-y-6">
          <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-sm space-y-6">
            <div>
              <h2 className="text-lg font-bold text-slate-900">Importación Masiva desde Excel (Apache POI)</h2>
              <p className="text-xs text-slate-500 mt-1">
                Sube archivos en formato .xlsx o .xls con validación de obligatoriedad y previsualización interactiva previa (RF-16).
              </p>
            </div>

            {/* Selector de Tipo de Carga */}
            <div className="flex p-1 bg-slate-100 rounded-xl max-w-md">
              <button
                type="button"
                onClick={() => { setTipoExcel('ESTUDIANTES'); setPreviewData(null); setSelectedFile(null); }}
                className={`flex-1 py-2 text-xs font-bold rounded-lg transition-all cursor-pointer ${
                  tipoExcel === 'ESTUDIANTES' ? 'bg-white text-blue-600 shadow-xs' : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                1. Excel de Estudiantes (RF-11)
              </button>
              <button
                type="button"
                onClick={() => { setTipoExcel('FECHAS'); setPreviewData(null); setSelectedFile(null); }}
                className={`flex-1 py-2 text-xs font-bold rounded-lg transition-all cursor-pointer ${
                  tipoExcel === 'FECHAS' ? 'bg-white text-indigo-600 shadow-xs' : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                2. Excel de Fechas / Cronograma (RF-12)
              </button>
            </div>

            {/* Drag & Drop Upload Zone */}
            <div className="border-2 border-dashed border-slate-300 hover:border-blue-500 rounded-2xl p-8 text-center transition-colors bg-slate-50/50">
              <Upload className="w-10 h-10 text-slate-400 mx-auto mb-3" />
              <h3 className="text-sm font-bold text-slate-800">
                Selecciona tu archivo Excel ({tipoExcel === 'ESTUDIANTES' ? 'Estudiantes' : 'Fechas de Clase'})
              </h3>
              <p className="text-xs text-slate-500 mt-1 mb-4">
                {tipoExcel === 'ESTUDIANTES'
                  ? 'Columnas esperadas: [Registro, Nombre Completo, Correo]'
                  : 'Columnas esperadas: [Fecha (YYYY-MM-DD), Hora Inicio (HH:mm), Hora Fin (HH:mm)]'}
              </p>
              <div className="flex flex-wrap items-center justify-center gap-3">
                <label className="inline-flex items-center gap-2 px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold rounded-xl cursor-pointer shadow-xs">
                  <Upload className="w-4 h-4" />
                  <span>Examinar archivo local (.xlsx)</span>
                  <input
                    type="file"
                    accept=".xlsx,.xls"
                    onChange={handleFileChange}
                    className="hidden"
                  />
                </label>

                {tipoExcel === 'ESTUDIANTES' ? (
                  <button
                    type="button"
                    onClick={() => api.descargarPlantillaEstudiantes()}
                    className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-xl border border-slate-300 transition-colors cursor-pointer"
                  >
                    <Download className="w-4 h-4 text-slate-500" />
                    <span>Descargar Plantilla Estudiantes (.xlsx)</span>
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={() => api.descargarPlantillaFechas()}
                    className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-xl border border-slate-300 transition-colors cursor-pointer"
                  >
                    <Download className="w-4 h-4 text-slate-500" />
                    <span>Descargar Plantilla Fechas (.xlsx)</span>
                  </button>
                )}
              </div>
            </div>

            {previewLoading && (
              <div className="p-8 text-center">
                <div className="w-8 h-8 border-3 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-2"></div>
                <p className="text-xs text-slate-500">Analizando estructura y validando datos del Excel...</p>
              </div>
            )}

            {/* PREVISUALIZACIÓN DE DATOS (RF-16) */}
            {previewData && (
              <div className="space-y-4 pt-4 border-t border-slate-200 animate-fadeIn">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-slate-100 p-4 rounded-xl">
                  <div>
                    <h3 className="font-bold text-sm text-slate-900 flex items-center gap-2">
                      <Eye className="w-4 h-4 text-blue-600" />
                      <span>Vista Previa: {previewData.nombreArchivo}</span>
                    </h3>
                    <div className="flex items-center gap-3 text-xs text-slate-600 mt-1">
                      <span>Total Filas: <strong>{previewData.totalFilas}</strong></span>
                      <span className="text-emerald-700">Válidas: <strong>{previewData.filasValidas}</strong></span>
                      {previewData.filasConError > 0 && (
                        <span className="text-rose-700">Con Errores: <strong>{previewData.filasConError}</strong></span>
                      )}
                    </div>
                  </div>

                  {/* Botón de Confirmación */}
                  <button
                    onClick={handleConfirmarImportacion}
                    disabled={importLoading || previewData.filasConError > 0}
                    className="flex items-center gap-2 px-6 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-xl shadow-md transition-all cursor-pointer disabled:opacity-40"
                  >
                    {importLoading ? 'Procesando en BD...' : 'Confirmar e Importar al Módulo'}
                    <ArrowRight className="w-4 h-4" />
                  </button>
                </div>

                {/* Reporte de Errores si los hay */}
                {previewData.errores && previewData.errores.length > 0 && (
                  <div className="p-4 bg-rose-50 border border-rose-200 rounded-xl space-y-1.5 text-xs text-rose-800">
                    <div className="flex items-center gap-1.5 font-bold text-rose-900">
                      <AlertTriangle className="w-4 h-4 text-rose-600" />
                      <span>Errores detectados que impiden la importación:</span>
                    </div>
                    <ul className="list-disc list-inside space-y-0.5 text-[11px] text-rose-700 pl-1">
                      {previewData.errores.slice(0, 5).map((err, i) => (
                        <li key={i}>{err}</li>
                      ))}
                      {previewData.errores.length > 5 && (
                        <li>... y {previewData.errores.length - 5} observaciones más.</li>
                      )}
                    </ul>
                  </div>
                )}

                {/* Tabla de Preview */}
                <div className="bg-white rounded-xl border border-slate-200 overflow-hidden max-h-72 overflow-y-auto">
                  <table className="w-full text-left text-xs">
                    <thead className="bg-slate-100 text-slate-700 font-bold sticky top-0">
                      <tr>
                        <th className="px-4 py-2.5">Fila</th>
                        {tipoExcel === 'ESTUDIANTES' ? (
                          <>
                            <th className="px-4 py-2.5">N° Registro</th>
                            <th className="px-4 py-2.5">Nombre Completo</th>
                            <th className="px-4 py-2.5">Correo</th>
                          </>
                        ) : (
                          <>
                            <th className="px-4 py-2.5">Fecha</th>
                            <th className="px-4 py-2.5">Hora Inicio</th>
                            <th className="px-4 py-2.5">Hora Fin</th>
                          </>
                        )}
                        <th className="px-4 py-2.5">Validación</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {previewData.datos.map((item, idx) => (
                        <tr key={idx} className={item.valida ? 'hover:bg-slate-50' : 'bg-rose-50/50'}>
                          <td className="px-4 py-2.5 font-semibold text-slate-400">{item.fila}</td>
                          {tipoExcel === 'ESTUDIANTES' ? (
                            <>
                              <td className="px-4 py-2.5 font-mono">{item.numeroRegistro}</td>
                              <td className="px-4 py-2.5 font-medium">{item.nombreCompleto}</td>
                              <td className="px-4 py-2.5">{item.correo}</td>
                            </>
                          ) : (
                            <>
                              <td className="px-4 py-2.5 font-bold">{item.fecha}</td>
                              <td className="px-4 py-2.5">{item.horaInicio}</td>
                              <td className="px-4 py-2.5">{item.horaFin}</td>
                            </>
                          )}
                          <td className="px-4 py-2.5">
                            {item.valida ? (
                              <span className="text-emerald-700 font-bold text-[11px] flex items-center gap-1">
                                <CheckCircle2 className="w-3.5 h-3.5" /> Válida
                              </span>
                            ) : (
                              <span className="text-rose-700 font-bold text-[11px] flex items-center gap-1">
                                <XCircle className="w-3.5 h-3.5" /> Error
                              </span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* TAB 4: AUDITORÍA Y ACTIVIDADES (RN-06, RN-07) */}
      {activeTab === 'AUDITORIA' && (
        <div className="space-y-8">
          {/* Tabla de Modificaciones Manuales de Asistencia (Auditoría) */}
          <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
            <div className="px-6 py-4 bg-slate-900 text-white flex items-center justify-between">
              <div>
                <h3 className="font-bold text-sm flex items-center gap-2">
                  <ShieldAlert className="w-4 h-4 text-amber-400" />
                  <span>Registro de Auditoría de Asistencias (RN-06/RN-07)</span>
                </h3>
                <p className="text-xs text-slate-400">Modificaciones manuales realizadas con usuario, hora y justificación obligatoria</p>
              </div>
              <span className="text-xs bg-slate-800 px-3 py-1 rounded-full text-amber-300 font-mono">
                {auditoriasList.length} registros
              </span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-100 text-slate-700 font-bold uppercase tracking-wider border-b border-slate-200">
                  <tr>
                    <th className="px-4 py-3">Fecha / Hora</th>
                    <th className="px-4 py-3">Usuario Admin</th>
                    <th className="px-4 py-3">Estudiante</th>
                    <th className="px-4 py-3">Sesión</th>
                    <th className="px-4 py-3 text-center">Cambio de Estado</th>
                    <th className="px-4 py-3">Motivo / Justificación</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {auditoriasList.map((a) => (
                    <tr key={a.id} className="hover:bg-slate-50 transition-colors">
                      <td className="px-4 py-3 font-mono text-slate-600 whitespace-nowrap">
                        {a.fechaHora?.replace('T', ' ').slice(0, 19)}
                      </td>
                      <td className="px-4 py-3 font-bold text-slate-800">{a.usuarioModificacion}</td>
                      <td className="px-4 py-3 font-semibold text-slate-900">
                        <div>{a.asistencia?.estudiante?.nombreCompleto}</div>
                        <div className="text-[10px] text-slate-400 font-mono">{a.asistencia?.estudiante?.numeroRegistro}</div>
                      </td>
                      <td className="px-4 py-3 text-slate-600 whitespace-nowrap">
                        {a.asistencia?.fechaClase?.fecha}
                      </td>
                      <td className="px-4 py-3 text-center whitespace-nowrap">
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-[10px] font-bold bg-slate-100 border border-slate-200">
                          <span className={a.valorAnterior === 'PRESENTE' ? 'text-emerald-700' : 'text-rose-700'}>
                            {a.valorAnterior}
                          </span>
                          <span className="text-slate-400">&rarr;</span>
                          <span className={a.valorNuevo === 'PRESENTE' ? 'text-emerald-700 font-black' : 'text-rose-700 font-black'}>
                            {a.valorNuevo}
                          </span>
                        </span>
                      </td>
                      <td className="px-4 py-3 text-slate-700 max-w-xs italic">
                        "{a.motivo}"
                      </td>
                    </tr>
                  ))}
                  {auditoriasList.length === 0 && (
                    <tr>
                      <td colSpan="6" className="px-6 py-12 text-center text-slate-400">
                        No se han registrado modificaciones manuales de asistencia en este módulo.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* Historial de Actividades del Sistema */}
          <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
            <div className="px-6 py-4 bg-slate-50 border-b border-slate-200 flex items-center justify-between">
              <div>
                <h3 className="font-bold text-sm text-slate-900">Bitácora de Eventos y Actividades del Módulo</h3>
                <p className="text-xs text-slate-500">Historial de cargas, inicios de sesión y operaciones del sistema</p>
              </div>
              <span className="text-xs bg-slate-200 px-3 py-1 rounded-full text-slate-700 font-mono">
                {actividadesList.length} eventos
              </span>
            </div>

            <div className="overflow-x-auto max-h-80 overflow-y-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-100 text-slate-700 font-bold sticky top-0">
                  <tr>
                    <th className="px-4 py-2.5">Fecha y Hora</th>
                    <th className="px-4 py-2.5">Usuario</th>
                    <th className="px-4 py-2.5">Acción</th>
                    <th className="px-4 py-2.5">Descripción de Registro</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {actividadesList.map((act) => (
                    <tr key={act.id} className="hover:bg-slate-50">
                      <td className="px-4 py-2.5 font-mono text-slate-500 whitespace-nowrap">
                        {act.fechaHora?.replace('T', ' ').slice(0, 19)}
                      </td>
                      <td className="px-4 py-2.5 font-bold text-slate-800">{act.usuario}</td>
                      <td className="px-4 py-2.5 font-mono text-[11px] text-blue-700 font-semibold">{act.accion}</td>
                      <td className="px-4 py-2.5 text-slate-600">{act.descripcionRegistro}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* TAB 5: GESTIÓN DE MÓDULOS HISTÓRICOS (RN-01) */}
      {activeTab === 'MODULOS' && (
        <div className="space-y-6">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-white p-4 rounded-2xl border border-slate-200">
            <div>
              <h2 className="text-sm font-bold text-slate-900">Historial de Módulos Registrados</h2>
              <p className="text-xs text-slate-500">
                Regla RN-01: Solo un módulo activo a la vez. Los datos anteriores se conservan íntegros en PostgreSQL.
              </p>
            </div>
            <button
              onClick={() => setShowNuevoModuloModal(true)}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold rounded-xl cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Activar Nuevo Módulo</span>
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {todosModulos.map((m) => (
              <div
                key={m.id}
                className={`p-6 rounded-2xl border transition-all ${
                  m.activo
                    ? 'bg-blue-50/40 border-blue-400 ring-2 ring-blue-500/10 shadow-md'
                    : 'bg-white border-slate-200 hover:border-slate-300'
                }`}
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="font-bold text-base text-slate-900">{m.nombre}</h3>
                      {m.activo ? (
                        <span className="px-2.5 py-0.5 rounded-full text-[10px] font-black uppercase bg-emerald-100 text-emerald-800 border border-emerald-300">
                          ACTIVO
                        </span>
                      ) : (
                        <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold uppercase bg-slate-100 text-slate-600">
                          Histórico (Inactivo)
                        </span>
                      )}
                    </div>
                    {m.codigo && <div className="text-xs font-mono text-slate-500 mt-0.5">Código: {m.codigo}</div>}
                  </div>
                </div>

                <div className="mt-4 pt-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
                  <div>
                    <span>Creado: {m.fechaCreacion?.slice(0, 10)}</span>
                    {m.fechaReinicio && <div className="text-[11px] text-amber-700">Reiniciado: {m.fechaReinicio.slice(0, 10)}</div>}
                  </div>

                  {!m.activo && (
                    <button
                      onClick={() => handleActivarModuloExistente(m.id)}
                      className="px-3 py-1.5 bg-slate-900 hover:bg-blue-600 text-white text-xs font-bold rounded-lg transition-colors cursor-pointer"
                    >
                      Establecer como Activo
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Modales */}
      {selectedCelda && (
        <ModalModificarAsistencia
          celdaData={selectedCelda.celdaData}
          estudiante={selectedCelda.estudiante}
          fecha={selectedCelda.fecha}
          user={user}
          onClose={() => setSelectedCelda(null)}
          onSuccess={(msg) => {
            if (onNotification) onNotification('success', msg);
            cargarMatriz();
            cargarAuditoriasYActividades();
          }}
        />
      )}

      {showNuevoModuloModal && (
        <ModalNuevoModulo
          user={user}
          onClose={() => setShowNuevoModuloModal(false)}
          onSuccess={(msg) => {
            if (onNotification) onNotification('success', msg);
            onRefreshModulo();
            cargarModulosHistoricos();
          }}
        />
      )}

      {showReinicioModal && (
        <ModalReinicioModulo
          moduloActivo={moduloActivo}
          user={user}
          onClose={() => setShowReinicioModal(false)}
          onSuccess={(msg) => {
            if (onNotification) onNotification('success', msg);
            onRefreshModulo();
            cargarModulosHistoricos();
          }}
        />
      )}
    </div>
  );
}
