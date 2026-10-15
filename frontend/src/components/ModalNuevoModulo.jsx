import React, { useState } from 'react';
import { PlusCircle, X, BookOpen, AlertCircle, Sparkles } from 'lucide-react';
import { api } from '../services/api';

export default function ModalNuevoModulo({ onClose, onSuccess, user }) {
  const [nombre, setNombre] = useState('');
  const [codigo, setCodigo] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!nombre.trim()) {
      setError('El nombre del módulo es obligatorio.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const res = await api.activarNuevoModulo(
        nombre.trim(),
        codigo.trim() || null,
        user?.nombre || user?.username || 'Administrador'
      );
      onSuccess(`Módulo '${res.data.nombre}' activado exitosamente.`);
      onClose();
    } catch (err) {
      setError(err.message || 'Error al activar el nuevo módulo');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-fadeIn">
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="px-6 py-4 bg-blue-600 text-white flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <BookOpen className="w-5 h-5 text-blue-100" />
            <h3 className="font-bold text-base">Activar Nuevo Módulo Académico</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-blue-200 hover:text-white hover:bg-blue-700 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div className="p-3 bg-blue-50 border border-blue-200 rounded-xl text-xs text-blue-800">
            <strong>Regla RN-01:</strong> Al activar este nuevo módulo, cualquier módulo previo pasará a estado inactivo pero todos sus datos se conservarán íntegros en PostgreSQL.
          </div>

          {error && (
            <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl text-xs text-rose-700">
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Nombre del Módulo <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              placeholder="Ej: Módulo II - Desarrollo Full Stack Avanzado"
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-300 rounded-xl text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white text-slate-900"
              required
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Código / Identificador (Opcional)
            </label>
            <input
              type="text"
              value={codigo}
              onChange={(e) => setCodigo(e.target.value)}
              placeholder="Ej: MOD-FULLSTACK-02"
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-300 rounded-xl text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white text-slate-900"
            />
          </div>

          {/* Quick presets */}
          <div className="pt-1">
            <span className="text-[11px] font-medium text-slate-500 block mb-1">Plantilla rápida sugerida:</span>
            <button
              type="button"
              onClick={() => {
                setNombre('Diplomado en Inteligencia Artificial y Machine Learning - Módulo I');
                setCodigo('MOD-IA-01');
              }}
              className="text-[11px] font-medium text-blue-600 hover:text-blue-800 bg-blue-50 hover:bg-blue-100 px-2 py-1 rounded-md transition-colors cursor-pointer"
            >
              + Usar: Diplomado en IA - Módulo I
            </button>
          </div>

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
              className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold rounded-xl shadow-md transition-all cursor-pointer disabled:opacity-50"
            >
              {loading ? 'Creando y Activando...' : 'Activar Módulo'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
