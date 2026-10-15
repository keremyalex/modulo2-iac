import React, { useState } from 'react';
import { AlertTriangle, RefreshCw, X, ShieldCheck } from 'lucide-react';
import { api } from '../services/api';

export default function ModalReinicioModulo({ moduloActivo, onClose, onSuccess, user }) {
  const [motivo, setMotivo] = useState('');
  const [confirmText, setConfirmText] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleReiniciar = async (e) => {
    e.preventDefault();
    if (confirmText.trim().toUpperCase() !== 'REINICIAR') {
      setError("Debe escribir la palabra 'REINICIAR' para confirmar la operación crítica.");
      return;
    }

    setLoading(true);
    setError('');

    try {
      await api.reiniciarModulo(motivo.trim() || 'Reinicio de ciclo de módulo', user?.nombre || user?.username || 'Administrador');
      onSuccess(`El módulo '${moduloActivo.nombre}' ha sido reiniciado. Sus datos históricos permanecen intactos en la base de datos.`);
      onClose();
    } catch (err) {
      setError(err.message || 'Error al reiniciar el módulo');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-fadeIn">
      <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="px-6 py-4 bg-amber-600 text-white flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <RefreshCw className="w-5 h-5 text-amber-100" />
            <h3 className="font-bold text-base">Reinicio de Módulo Académico (RN-08)</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-amber-200 hover:text-white hover:bg-amber-700 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleReiniciar} className="p-6 space-y-4">
          <div className="p-4 bg-amber-50 rounded-xl border border-amber-200 text-xs text-amber-900 space-y-2">
            <div className="flex items-center gap-2 font-bold text-amber-800">
              <AlertTriangle className="w-4 h-4 text-amber-600 shrink-0" />
              <span>Confirmación de Acción Operativa Crítica</span>
            </div>
            <p>
              Estás a punto de finalizar y reiniciar el módulo activo: <strong className="underline">{moduloActivo?.nombre}</strong>.
            </p>
            <div className="flex items-start gap-2 pt-1 text-slate-700">
              <ShieldCheck className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
              <span>
                <strong>Garantía de Persistencia (RN-01):</strong> Los estudiantes, fechas de clase, marcajes y auditorías anteriores <strong>se conservan de forma permanente</strong> en la base de datos PostgreSQL, pasando este módulo a estado inactivo para el inicio de un nuevo ciclo.
              </span>
            </div>
          </div>

          {error && (
            <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl text-xs text-rose-700">
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Motivo del Reinicio
            </label>
            <input
              type="text"
              value={motivo}
              onChange={(e) => setMotivo(e.target.value)}
              placeholder="Ej: Finalización de ciclo lectivo 2026-I y preparación de nuevo módulo"
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-300 rounded-xl text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:bg-white text-slate-900"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Para confirmar, escribe <span className="text-amber-700 font-mono font-black">REINICIAR</span>
            </label>
            <input
              type="text"
              value={confirmText}
              onChange={(e) => setConfirmText(e.target.value)}
              placeholder="REINICIAR"
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-300 rounded-xl text-xs sm:text-sm font-mono focus:outline-none focus:ring-2 focus:ring-amber-500 focus:bg-white text-slate-900 uppercase"
              required
            />
          </div>

          {/* Buttons */}
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
              disabled={loading || confirmText.trim().toUpperCase() !== 'REINICIAR'}
              className="px-5 py-2.5 bg-amber-600 hover:bg-amber-700 text-white text-xs font-bold rounded-xl shadow-md transition-all cursor-pointer disabled:opacity-40"
            >
              {loading ? 'Reiniciando...' : 'Confirmar Reinicio de Módulo'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
