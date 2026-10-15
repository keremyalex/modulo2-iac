import React, { useState } from 'react';
import { GraduationCap, Shield, User, ArrowRight, Lock, Mail, AlertCircle, CheckCircle2, Sparkles } from 'lucide-react';
import { api } from '../services/api';

export default function LoginView({ onLoginSuccess, moduloActivo }) {
  const [roleTab, setRoleTab] = useState('ESTUDIANTE'); // 'ESTUDIANTE' | 'ADMIN'
  const [identificador, setIdentificador] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleStudentSubmit = async (e) => {
    e.preventDefault();
    if (!identificador.trim()) {
      setError('Por favor ingrese su número de registro o correo institucional.');
      return;
    }
    setError('');
    setLoading(true);
    try {
      const res = await api.loginEstudiante(identificador.trim());
      onLoginSuccess(res.data);
    } catch (err) {
      setError(err.message || 'Error al iniciar sesión como estudiante');
    } finally {
      setLoading(false);
    }
  };

  const handleAdminSubmit = async (e) => {
    e.preventDefault();
    if (!username.trim() || !password.trim()) {
      setError('Por favor ingrese su usuario y contraseña de administrador.');
      return;
    }
    setError('');
    setLoading(true);
    try {
      const res = await api.loginAdmin(username.trim(), password.trim());
      onLoginSuccess(res.data);
    } catch (err) {
      setError(err.message || 'Error al iniciar sesión como administrador');
    } finally {
      setLoading(false);
    }
  };

  const fillStudentDemo = (id) => {
    setIdentificador(id);
    setError('');
  };

  const fillAdminDemo = () => {
    setUsername('admin');
    setPassword('admin123');
    setError('');
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-indigo-950 flex flex-col justify-center items-center px-4 py-12 relative overflow-hidden">
      {/* Background ambient lighting */}
      <div className="absolute top-1/4 -left-32 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl pointer-events-none"></div>
      <div className="absolute bottom-1/4 -right-32 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none"></div>

      <div className="w-full max-w-md z-10">
        {/* Header Branding */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-tr from-blue-500 to-indigo-600 text-white shadow-xl shadow-blue-500/25 mb-4">
            <GraduationCap className="w-9 h-9" />
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            Control de Asistencia
          </h1>
          <p className="text-sm text-slate-400 mt-1 font-normal">
            Sistema Académico para Diplomados y Cursos de Posgrado
          </p>

          {moduloActivo && (
            <div className="mt-4 inline-flex items-center gap-2 px-3 py-1 bg-blue-500/10 border border-blue-500/20 rounded-full text-xs text-blue-300">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              <span>Módulo: <strong>{moduloActivo.nombre}</strong></span>
            </div>
          )}
        </div>

        {/* Main Card */}
        <div className="bg-white/95 backdrop-blur-xl rounded-2xl shadow-2xl border border-white/20 p-6 sm:p-8">
          {/* Dual Role Selector Tabs */}
          <div className="flex p-1 bg-slate-100 rounded-xl mb-6">
            <button
              type="button"
              onClick={() => { setRoleTab('ESTUDIANTE'); setError(''); }}
              className={`flex-1 flex items-center justify-center gap-2 py-2.5 text-xs sm:text-sm font-semibold rounded-lg transition-all cursor-pointer ${
                roleTab === 'ESTUDIANTE'
                  ? 'bg-white text-blue-600 shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <User className="w-4 h-4" />
              Portal Estudiante
            </button>
            <button
              type="button"
              onClick={() => { setRoleTab('ADMIN'); setError(''); }}
              className={`flex-1 flex items-center justify-center gap-2 py-2.5 text-xs sm:text-sm font-semibold rounded-lg transition-all cursor-pointer ${
                roleTab === 'ADMIN'
                  ? 'bg-white text-indigo-600 shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <Shield className="w-4 h-4" />
              Administrador
            </button>
          </div>

          {/* Error Banner */}
          {error && (
            <div className="mb-5 p-3.5 bg-rose-50 border border-rose-200 rounded-xl flex items-start gap-3 text-xs text-rose-700 animate-fadeIn">
              <AlertCircle className="w-4 h-4 shrink-0 text-rose-600 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          {/* Form Content */}
          {roleTab === 'ESTUDIANTE' ? (
            <form onSubmit={handleStudentSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                  Identificación de Estudiante
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <Mail className="w-4 h-4" />
                  </div>
                  <input
                    type="text"
                    value={identificador}
                    onChange={(e) => setIdentificador(e.target.value)}
                    placeholder="Ej: EST-1001 o carlos.perez@diplomado.edu"
                    className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all text-slate-900"
                    disabled={loading}
                  />
                </div>
                <p className="text-[11px] text-slate-500 mt-1">
                  Ingresa con tu correo registrado o tu número de carnet/matrícula asignado en el módulo activo.
                </p>
              </div>

              {/* Quick demo pills */}
              <div className="pt-2">
                <span className="text-[11px] font-medium text-slate-500 block mb-1.5">Acceso rápido de prueba:</span>
                <div className="flex flex-wrap gap-1.5">
                  <button
                    type="button"
                    onClick={() => fillStudentDemo('EST-1001')}
                    className="px-2 py-1 text-[11px] font-medium bg-slate-100 hover:bg-blue-50 hover:text-blue-700 border border-slate-200 rounded-md transition-colors cursor-pointer"
                  >
                    Carlos Pérez (EST-1001)
                  </button>
                  <button
                    type="button"
                    onClick={() => fillStudentDemo('EST-1002')}
                    className="px-2 py-1 text-[11px] font-medium bg-slate-100 hover:bg-blue-50 hover:text-blue-700 border border-slate-200 rounded-md transition-colors cursor-pointer"
                  >
                    Mariana Morales (EST-1002)
                  </button>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full mt-4 flex items-center justify-center gap-2 py-3 px-4 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white text-sm font-semibold rounded-xl shadow-md shadow-blue-500/25 transition-all cursor-pointer disabled:opacity-50"
              >
                {loading ? (
                  <span className="inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                ) : (
                  <>
                    <span>Ingresar al Portal de Asistencia</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>
          ) : (
            <form onSubmit={handleAdminSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                  Usuario Administrador
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <User className="w-4 h-4" />
                  </div>
                  <input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="admin"
                    className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white transition-all text-slate-900"
                    disabled={loading}
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                  Contraseña
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <Lock className="w-4 h-4" />
                  </div>
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white transition-all text-slate-900"
                    disabled={loading}
                  />
                </div>
              </div>

              {/* Demo autofill */}
              <div className="pt-1">
                <button
                  type="button"
                  onClick={fillAdminDemo}
                  className="inline-flex items-center gap-1.5 text-xs text-indigo-600 hover:text-indigo-800 font-medium cursor-pointer"
                >
                  <Sparkles className="w-3.5 h-3.5" />
                  <span>Rellenar credenciales demo (admin / admin123)</span>
                </button>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full mt-4 flex items-center justify-center gap-2 py-3 px-4 bg-gradient-to-r from-indigo-600 to-slate-900 hover:from-indigo-700 hover:to-slate-800 text-white text-sm font-semibold rounded-xl shadow-md shadow-indigo-500/25 transition-all cursor-pointer disabled:opacity-50"
              >
                {loading ? (
                  <span className="inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                ) : (
                  <>
                    <span>Acceder al Panel de Control</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>
          )}
        </div>

        {/* Footer info */}
        <p className="text-center text-xs text-slate-400 mt-6">
          Gestión Segura con PostgreSQL &bull; Módulos Académicos Independientes
        </p>
      </div>
    </div>
  );
}
