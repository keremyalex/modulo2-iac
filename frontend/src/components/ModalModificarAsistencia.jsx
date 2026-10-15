import React, { useState } from 'react';
import { ShieldAlert, X, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { api } from '../services/api';

export default function ModalModificarAsistencia({ celdaData, estudiante, fecha, onClose, onSuccess, user }) {
  const [nuevoEstado, setNuevoEstado] = useState(
    celdaData.estado === 'PRESENTE' ? 'FALTA' : 'PRESENTE'
  );
  const [motivo, setMotivo] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!motivo.trim()) {
      setError('El motivo o justificación es obligatorio para el registro de auditoría (RN-06).');
      return;
    }

    setLoading(true);
    setError('');

    try {
      await api.modificarAsistencia(
        celdaData.asistenciaId,
        nuevoEstado,
        motivo.trim(),
        user?.nombre || user?.username || 'Administrador'
      );
      onSuccess(`Asistencia de ${estudiante.nombreCompleto} actualizada a ${nuevoEstado} exitosamente.`);
      onClose();
    } catch (err) {
      setError(err.message || 'Error al modificar la asistencia');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-fadeIn">
      <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="px-6 py-4 bg-slate-900 text-white flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <ShieldAlert className="w-5 h-5 text-amber-400" />
            <h3 className="font-bold text-base">Modificación Administrativa de Asistencia</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* Target Student Info Card */}
          <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200 text-xs space-y-1">
            <div className="font-bold text-slate-900 text-sm">{estudiante.nombreCompleto}</div>
            <div className="text-slate-600 flex items-center justify-between">
              <span>Registro: <strong>{estudiante.numeroRegistro}</strong></span>
              <span>Fecha: <strong>{fecha.fecha}</strong></span>
            </div>
            <div className="text-slate-500">
              Estado actual: <strong className={celdaData.estado === 'PRESENTE' ? 'text-emerald-600' : 'text-rose-600'}>
                {celdaData.estado}
              </strong>
            </div>
          </div>

          {/* Audit Rule Reminder */}
          <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl flex items-start gap-2.5 text-xs text-amber-800">
            <AlertTriangle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
            <span>
              <strong>Auditoría Obligatoria (RN-06/RN-07):</strong> Esta modificación quedará registrada con tu usuario, fecha y hora exacta, conservando el valor anterior.
            </span>
          </div>

          {error && (
            <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl text-xs text-rose-700">
              {error}
            </div>
          )}

          {/* New State Selector */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
              Nuevo Estado de Asistencia
            </label>
            <div className="grid grid-cols-2 gap-3">
              <button
                type="button"
                onClick={() => setNuevoEstado('PRESENTE')}
                className={`py-2.5 px-4 rounded-xl font-bold text-xs border flex items-center justify-center gap-2 cursor-pointer transition-all ${
                  nuevoEstado === 'PRESENTE'
                    ? 'bg-emerald-50 border-emerald-500 text-emerald-700 ring-2 ring-emerald-500/20'
                    : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                PRESENTE
              </button>
              <button
                type="button"
                onClick={() => setNuevoEstado('FALTA')}
                className={`py-2.5 px-4 rounded-xl font-bold text-xs border flex items-center justify-center gap-2 cursor-pointer transition-all ${
                  nuevoEstado === 'FALTA'
                    ? 'bg-rose-50 border-rose-500 text-rose-700 ring-2 ring-rose-500/20'
                    : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                <span className="w-3.5 h-3.5 rounded-full bg-rose-500 text-white flex items-center justify-center text-[10px] font-bold">✕</span>
                FALTA
              </button>
            </div>
          </div>

          {/* Motivo Input */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Motivo o Justificación <span className="text-rose-500">*</span>
            </label>
            <textarea
              rows="3"
              value={motivo}
              onChange={(e) => setMotivo(e.target.value)}
              placeholder="Ej: Justificante médico presentado y validado por dirección académica."
              className="w-full p-3 bg-slate-50 border border-slate-300 rounded-xl text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white text-slate-900 resize-none"
              required
            />
          </div>

          {/* Action buttons */}
          <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2.5 text-xs font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-100 rounded-xl transition-colors cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-xl shadow-md transition-all cursor-pointer disabled:opacity-50"
            >
              {loading ? 'Guardando en auditoría...' : 'Confirmar y Guardar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
