import React from 'react';
import { GraduationCap, Shield, User, LogOut, RefreshCw, BookOpen } from 'lucide-react';

export default function Navbar({ user, moduloActivo, onLogout, onOpenReinicioModal }) {
  const isAdmin = user?.rol === 'ADMIN';

  return (
    <header className="sticky top-0 z-40 bg-white/90 backdrop-blur-md border-b border-slate-200 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand / Logo */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center text-white shadow-md shadow-blue-500/20">
            <GraduationCap className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-lg text-slate-900 tracking-tight">AsistenciaDiplomado</span>
              <span className="text-xs px-2 py-0.5 rounded-full font-medium bg-blue-100 text-blue-700">v1.0</span>
            </div>
            <p className="text-xs text-slate-500 hidden sm:block">Control & Registro Académico</p>
          </div>
        </div>

        {/* Center: Módulo Activo Info */}
        {moduloActivo && (
          <div className="hidden md:flex items-center gap-2 px-3 py-1.5 bg-slate-100 rounded-lg border border-slate-200 text-xs">
            <BookOpen className="w-4 h-4 text-blue-600" />
            <span className="text-slate-500 font-medium">Módulo Activo:</span>
            <span className="font-semibold text-slate-800 max-w-xs truncate">{moduloActivo.nombre}</span>
            <span className="inline-block w-2 h-2 rounded-full bg-emerald-500 animate-pulse" title="Módulo Operativo"></span>
          </div>
        )}

        {/* User profile & actions */}
        <div className="flex items-center gap-3">
          {isAdmin && moduloActivo && onOpenReinicioModal && (
            <button
              onClick={onOpenReinicioModal}
              title="Reiniciar ciclo operativo del módulo actual"
              className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-amber-700 bg-amber-50 hover:bg-amber-100 border border-amber-200 rounded-lg transition-colors cursor-pointer"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">Reiniciar Módulo</span>
            </button>
          )}

          <div className="flex items-center gap-2 pl-2 border-l border-slate-200">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-semibold ${
              isAdmin ? 'bg-indigo-100 text-indigo-700 border border-indigo-200' : 'bg-blue-100 text-blue-700 border border-blue-200'
            }`}>
              {isAdmin ? <Shield className="w-4 h-4" /> : <User className="w-4 h-4" />}
            </div>
            <div className="text-left hidden sm:block">
              <div className="text-xs font-bold text-slate-800 leading-tight">{user?.nombre || user?.username}</div>
              <div className="text-[10px] uppercase font-semibold text-slate-500 tracking-wider">
                {user?.rol === 'ADMIN' ? 'Administrador' : 'Estudiante'}
              </div>
            </div>

            <button
              onClick={onLogout}
              title="Cerrar Sesión"
              className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors ml-1 cursor-pointer"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
