import React, { useState, useEffect } from 'react';
import { Calendar, Clock, CheckCircle2, XCircle, AlertCircle, Sparkles, RefreshCw, Award, BookOpen, User } from 'lucide-react';
import { api } from '../services/api';

export default function EstudianteView({ user, onNotification }) {
  const [loading, setLoading] = useState(true);
  const [marcando, setMarcando] = useState(false);
  const [resumen, setResumen] = useState(null);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const fetchResumen = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.getEstudianteResumen(user.estudianteId);
      setResumen(res.data);
    } catch (err) {
      setError(err.message || 'Error al obtener su historial de asistencias');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.estudianteId) {
      fetchResumen();
    }
  }, [user]);

  const handleMarcar = async () => {
    try {
      setMarcando(true);
      setError('');
      setSuccessMessage('');
      const res = await api.marcarAsistencia(user.estudianteId);
      setSuccessMessage(res.message || '¡Asistencia registrada exitosamente!');
      if (onNotification) onNotification('success', res.message);
      // Actualizar datos
      await fetchResumen();
    } catch (err) {
      setError(err.message || 'No se pudo registrar la asistencia');
      if (onNotification) onNotification('error', err.message);
    } finally {
      setMarcando(false);
    }
  };

  if (loading && !resumen) {
    return (
      <div className="flex-1 flex items-center justify-center py-24">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
          <p className="text-sm text-slate-500 font-medium">Cargando su expediente de asistencia...</p>
        </div>
      </div>
    );
  }

  const estudiante = resumen?.estudiante;
  const claseHoy = resumen?.claseHoy;
  const historial = resumen?.historial || [];

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Student Welcome Card */}
      <div className="relative overflow-hidden bg-gradient-to-r from-blue-700 via-indigo-700 to-indigo-900 rounded-2xl shadow-xl text-white p-6 sm:p-8">
        <div className="absolute top-0 right-0 -mr-16 -mt-16 w-64 h-64 bg-white/10 rounded-full blur-2xl pointer-events-none"></div>

        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/15 backdrop-blur-md rounded-full text-xs font-semibold tracking-wide uppercase text-blue-100">
              <BookOpen className="w-3.5 h-3.5" />
              <span>{estudiante?.modulo?.nombre || 'Módulo Académico'}</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
              Bienvenido, {estudiante?.nombreCompleto}
            </h1>
            <div className="flex flex-wrap items-center gap-4 text-xs sm:text-sm text-blue-100 font-normal">
              <span className="flex items-center gap-1.5">
                <User className="w-4 h-4 opacity-75" />
                N° Registro: <strong className="font-semibold">{estudiante?.numeroRegistro}</strong>
              </span>
              <span>&bull;</span>
              <span>{estudiante?.correo}</span>
            </div>
          </div>

          {/* Quick Attendance Percentage Badge */}
          <div className="bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl p-4 flex items-center gap-4 shrink-0">
            <div className="w-14 h-14 rounded-xl bg-white/20 flex items-center justify-center text-white">
              <Award className="w-8 h-8" />
            </div>
            <div>
              <div className="text-2xl sm:text-3xl font-black">{resumen?.porcentajeAsistencia || 0}%</div>
              <div className="text-xs text-blue-200">Asistencia Acumulada</div>
            </div>
          </div>
        </div>
      </div>

      {/* Notifications */}
      {successMessage && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-xl flex items-center gap-3 text-sm text-emerald-800 shadow-xs animate-fadeIn">
          <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
          <span>{successMessage}</span>
        </div>
      )}

      {error && (
        <div className="p-4 bg-rose-50 border border-rose-200 rounded-xl flex items-center gap-3 text-sm text-rose-800 shadow-xs animate-fadeIn">
          <AlertCircle className="w-5 h-5 text-rose-600 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Interactive Hero Card: Clase Programada de Hoy */}
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 sm:p-8">
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 mb-6">
          <div className="flex items-center gap-2">
            <Clock className="w-5 h-5 text-blue-600" />
            <h2 className="text-lg font-bold text-slate-900">Estado de la Clase de Hoy</h2>
          </div>
          <button
            onClick={fetchResumen}
            disabled={loading}
            title="Refrescar estado"
            className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors cursor-pointer"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>

        {claseHoy ? (
          <div className="flex flex-col md:flex-row items-center justify-between gap-6 p-6 rounded-2xl bg-slate-50 border border-slate-200/80">
            <div className="space-y-2 text-center md:text-left">
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-blue-100 text-blue-700">
                <Calendar className="w-3.5 h-3.5" />
                <span>Fecha: {claseHoy.fecha} (HOY)</span>
              </div>
              <h3 className="text-xl font-bold text-slate-800">
                Sesión Programada: {claseHoy.horaInicio} - {claseHoy.horaFin}
              </h3>
              <p className="text-xs sm:text-sm text-slate-500">
                {claseHoy.yaMarco
                  ? `Marcaje confirmado exitosamente a las ${claseHoy.horaMarcaje}`
                  : 'Recuerda registrar tu asistencia durante el desarrollo de la sesión académica.'}
              </p>
            </div>

            {/* Action Punch Button */}
            <div className="shrink-0">
              {claseHoy.yaMarco ? (
                <div className="flex items-center gap-2 px-6 py-3.5 bg-emerald-500 text-white rounded-xl shadow-md font-semibold text-sm">
                  <CheckCircle2 className="w-5 h-5" />
                  <span>Asistencia Registrada ({claseHoy.horaMarcaje})</span>
                </div>
              ) : claseHoy.puedeMarcar ? (
                <button
                  onClick={handleMarcar}
                  disabled={marcando}
                  className="group relative flex items-center gap-3 px-8 py-4 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white text-base font-bold rounded-2xl shadow-lg shadow-emerald-600/30 transition-all hover:scale-[1.02] active:scale-[0.98] cursor-pointer disabled:opacity-50"
                >
                  <span className="w-3 h-3 rounded-full bg-white animate-ping"></span>
                  <span>{marcando ? 'Registrando marca...' : '¡Marcar Mi Asistencia Ahora!'}</span>
                  <Sparkles className="w-5 h-5 opacity-80 group-hover:rotate-12 transition-transform" />
                </button>
              ) : (
                <div className="px-5 py-3 bg-slate-200 text-slate-600 rounded-xl font-medium text-xs">
                  Marcaje no habilitado fuera de horario programado
                </div>
              )}
            </div>
          </div>
        ) : (
          <div className="p-8 text-center bg-slate-50 rounded-2xl border border-dashed border-slate-300">
            <Calendar className="w-12 h-12 text-slate-400 mx-auto mb-3" />
            <h3 className="text-base font-bold text-slate-700">Hoy no hay clases programadas</h3>
            <p className="text-xs text-slate-500 max-w-md mx-auto mt-1">
              Las sesiones académicas se habilitan exclusivamente en los días y horarios definidos en el cronograma del módulo.
            </p>
          </div>
        )}
      </div>

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
          <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Total Sesiones</div>
          <div className="text-2xl font-bold text-slate-900">{resumen?.totalClases || 0}</div>
          <div className="text-[11px] text-slate-400 mt-1">Planificadas en el módulo</div>
        </div>
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
          <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Evaluadas a la Fecha</div>
          <div className="text-2xl font-bold text-slate-900">{resumen?.clasesTranscurridas || 0}</div>
          <div className="text-[11px] text-slate-400 mt-1">Clases ya transcurridas</div>
        </div>
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
          <div className="text-xs font-semibold text-emerald-600 uppercase tracking-wider mb-1">Presentes</div>
          <div className="text-2xl font-bold text-emerald-600">{resumen?.presentes || 0}</div>
          <div className="text-[11px] text-emerald-700/70 mt-1">Marcajes confirmados</div>
        </div>
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
          <div className="text-xs font-semibold text-rose-600 uppercase tracking-wider mb-1">Faltas</div>
          <div className="text-2xl font-bold text-rose-600">{resumen?.faltas || 0}</div>
          <div className="text-[11px] text-rose-700/70 mt-1">Inasistencias registradas</div>
        </div>
      </div>

      {/* Personal Attendance History Table */}
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
        <div className="px-6 py-5 border-b border-slate-200 bg-slate-50/50 flex items-center justify-between">
          <div>
            <h2 className="text-base font-bold text-slate-900">Historial Detallado de Asistencias</h2>
            <p className="text-xs text-slate-500 mt-0.5">Registro oficial e histórico de sesiones del módulo activo</p>
          </div>
          <span className="text-xs font-semibold text-slate-600 bg-slate-200/60 px-3 py-1 rounded-full">
            {historial.length} sesiones
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-100/70 text-slate-700 font-semibold text-xs uppercase tracking-wider border-b border-slate-200">
              <tr>
                <th className="px-6 py-3.5">N°</th>
                <th className="px-6 py-3.5">Fecha de Sesión</th>
                <th className="px-6 py-3.5">Horario Programado</th>
                <th className="px-6 py-3.5">Estado</th>
                <th className="px-6 py-3.5">Hora de Marcaje</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {historial.map((item, idx) => {
                const isPresente = item.estado === 'PRESENTE';
                return (
                  <tr key={item.fechaClaseId} className={`hover:bg-slate-50/80 transition-colors ${item.esHoy ? 'bg-blue-50/30' : ''}`}>
                    <td className="px-6 py-4 text-xs font-semibold text-slate-400">{idx + 1}</td>
                    <td className="px-6 py-4 font-semibold text-slate-800">
                      <div className="flex items-center gap-2">
                        <span>{item.fecha}</span>
                        {item.esHoy && (
                          <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-blue-100 text-blue-700 uppercase">
                            Hoy
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4 text-slate-600 text-xs">
                      {item.horaInicio} - {item.horaFin}
                    </td>
                    <td className="px-6 py-4">
                      {isPresente ? (
                        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800 border border-emerald-200">
                          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                          PRESENTE
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-rose-100 text-rose-800 border border-rose-200">
                          <XCircle className="w-3.5 h-3.5 text-rose-600" />
                          FALTA
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-xs font-mono text-slate-600">
                      {item.horaMarcaje ? item.horaMarcaje : <span className="text-slate-400 italic">Sin registro</span>}
                    </td>
                  </tr>
                );
              })}
              {historial.length === 0 && (
                <tr>
                  <td colSpan="5" className="px-6 py-12 text-center text-slate-400 text-sm">
                    No se han programado sesiones de clase para este módulo aún.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
