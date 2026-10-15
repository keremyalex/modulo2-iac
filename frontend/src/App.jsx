import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import LoginView from './components/LoginView';
import EstudianteView from './components/EstudianteView';
import AdminView from './components/AdminView';
import ModalReinicioModulo from './components/ModalReinicioModulo';
import { api } from './services/api';
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react';

export default function App() {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('asistencia_user');
    return saved ? JSON.parse(saved) : null;
  });

  const [moduloActivo, setModuloActivo] = useState(null);
  const [loadingModulo, setLoadingModulo] = useState(true);
  const [showReinicioModal, setShowReinicioModal] = useState(false);
  const [notification, setNotification] = useState(null); // { type: 'success' | 'error' | 'info', message: string }

  const notify = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => {
      setNotification((prev) => (prev?.message === message ? null : prev));
    }, 4500);
  };

  const fetchModuloActivo = async () => {
    try {
      setLoadingModulo(true);
      const res = await api.getModuloActivo();
      setModuloActivo(res.data);
    } catch (err) {
      console.error('Error fetching modulo activo:', err);
    } finally {
      setLoadingModulo(false);
    }
  };

  useEffect(() => {
    fetchModuloActivo();
  }, []);

  const handleLoginSuccess = (userData) => {
    setUser(userData);
    localStorage.setItem('asistencia_user', JSON.stringify(userData));
    notify('success', `¡Bienvenido(a), ${userData.nombre || userData.username}!`);
    fetchModuloActivo();
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('asistencia_user');
    notify('info', 'Sesión finalizada con éxito.');
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      {/* Toast Notification Container */}
      {notification && (
        <div className="fixed bottom-6 right-6 z-50 animate-bounce-subtle max-w-md">
          <div className={`p-4 rounded-2xl shadow-xl border flex items-start gap-3 backdrop-blur-md ${
            notification.type === 'success'
              ? 'bg-emerald-900/90 text-white border-emerald-500/30'
              : notification.type === 'error'
              ? 'bg-rose-900/90 text-white border-rose-500/30'
              : 'bg-slate-900/90 text-white border-slate-700'
          }`}>
            {notification.type === 'success' && <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />}
            {notification.type === 'error' && <AlertCircle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />}
            {notification.type === 'info' && <Info className="w-5 h-5 text-blue-400 shrink-0 mt-0.5" />}
            <span className="text-xs sm:text-sm font-medium">{notification.message}</span>
            <button
              onClick={() => setNotification(null)}
              className="text-white/60 hover:text-white p-0.5 ml-auto"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* Main Content Router */}
      {!user ? (
        <LoginView
          moduloActivo={moduloActivo}
          onLoginSuccess={handleLoginSuccess}
        />
      ) : (
        <>
          <Navbar
            user={user}
            moduloActivo={moduloActivo}
            onLogout={handleLogout}
            onOpenReinicioModal={() => setShowReinicioModal(true)}
          />

          <main className="flex-1 pb-16">
            {user.rol === 'ESTUDIANTE' ? (
              <EstudianteView
                user={user}
                onNotification={notify}
              />
            ) : (
              <AdminView
                user={user}
                moduloActivo={moduloActivo}
                onRefreshModulo={fetchModuloActivo}
                onNotification={notify}
              />
            )}
          </main>

          {/* Footer */}
          <footer className="border-t border-slate-200 bg-white py-6 text-center text-xs text-slate-500">
            <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
              <p>Sistema Académico de Control de Asistencia &copy; 2026</p>
              <p className="text-[11px] text-slate-400">
                Spring Boot (Java) &bull; PostgreSQL &bull; React &bull; Apache POI
              </p>
            </div>
          </footer>

          {/* Modal de reinicio rápido desde Navbar */}
          {showReinicioModal && moduloActivo && (
            <ModalReinicioModulo
              moduloActivo={moduloActivo}
              user={user}
              onClose={() => setShowReinicioModal(false)}
              onSuccess={(msg) => {
                notify('success', msg);
                fetchModuloActivo();
              }}
            />
          )}
        </>
      )}
    </div>
  );
}
