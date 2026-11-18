import React, { useState } from "react";
import { Download, Smartphone, Share, MoreVertical, X, CheckCircle2 } from "lucide-react";
import { usePWAInstall } from "../hooks/usePWAInstall";

interface PWAInstallButtonProps {
  variant?: "header" | "card";
}

export const PWAInstallButton: React.FC<PWAInstallButtonProps> = ({ variant = "header" }) => {
  const { isInstallable, isInstalled, isIOS, isAndroid, install } = usePWAInstall();
  const [showGuideModal, setShowGuideModal] = useState(false);

  const handleInstallClick = async () => {
    if (isInstallable) {
      const accepted = await install();
      if (!accepted) {
        setShowGuideModal(true);
      }
      return;
    }
    setShowGuideModal(true);
  };

  if (isInstalled) {
    if (variant === "card") {
      return (
        <div className="p-3.5 bg-emerald-50 border border-emerald-200 rounded-2xl flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-emerald-600 text-white flex items-center justify-center shrink-0">
            <CheckCircle2 className="w-5 h-5" />
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-xs font-black text-emerald-900 uppercase tracking-tight">
              Berjalan Sebagai Aplikasi Android
            </p>
            <p className="text-[11px] text-emerald-700 font-medium leading-snug">
              Mode layar penuh (Standalone App) aktif dengan cache offline.
            </p>
          </div>
        </div>
      );
    }
    return null;
  }

  return (
    <>
      {variant === "header" ? (
        <button
          type="button"
          onClick={handleInstallClick}
          className="flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-black border bg-mandiri-blue-600 text-white border-mandiri-blue-700 hover:bg-mandiri-blue-700 shadow-xs transition-all cursor-pointer active:scale-95 shrink-0"
          title="Pasang sebagai Aplikasi Android di Layar Utama"
        >
          <Smartphone className="w-3 h-3 shrink-0" />
          <span className="hidden xs:inline sm:inline">Pasang App</span>
          <span className="xs:hidden sm:hidden">App</span>
        </button>
      ) : (
        <div className="p-4 bg-gradient-to-br from-mandiri-blue-700 via-mandiri-blue-600 to-indigo-700 rounded-2xl text-white shadow-lg shadow-mandiri-blue-900/20 border border-mandiri-blue-400/30 space-y-3">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-white/15 backdrop-blur-sm border border-white/25 flex items-center justify-center shrink-0">
              <img src="/pwa-192x192.png" alt="G-Swift App Icon" className="w-8 h-8 rounded-lg object-cover" />
            </div>
            <div className="flex-1 min-w-0">
              <h4 className="text-xs font-black uppercase tracking-wider text-white">
                Aplikasi Android (G-Swift)
              </h4>
              <p className="text-[11px] text-blue-100 leading-snug mt-0.5">
                {isInstallable
                  ? "Siap dipasang langsung ke Layar Utama & App Drawer Android Anda."
                  : "Pasang sebagai aplikasi Android layar penuh dengan dukungan mode offline."}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={handleInstallClick}
            className="w-full py-2.5 px-4 bg-white text-mandiri-blue-800 hover:bg-blue-50 rounded-xl text-xs font-black uppercase tracking-wider transition-all cursor-pointer flex items-center justify-center gap-2 shadow-sm active:scale-[0.98]"
          >
            <Download className="w-4 h-4" />
            <span>{isInstallable ? "Install Aplikasi Android Sekarang" : "Cara Pasang di HP Android"}</span>
          </button>
        </div>
      )}

      {showGuideModal && (
        <div
          className="fixed inset-0 z-[130] bg-slate-900/65 backdrop-blur-xs flex items-center justify-center p-4"
          onClick={() => setShowGuideModal(false)}
        >
          <div
            className="bg-white rounded-3xl p-5 sm:p-6 w-full max-w-sm border border-slate-200 shadow-2xl space-y-4"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <img
                  src="/pwa-192x192.png"
                  alt="G-Swift Icon"
                  className="w-10 h-10 rounded-2xl shadow-sm border border-slate-200"
                />
                <div>
                  <h3 className="text-sm font-black text-slate-900 uppercase tracking-tight">
                    Pasang Aplikasi {isIOS ? "iOS" : "Android"}
                  </h3>
                  <p className="text-[11px] text-slate-500 font-medium">
                    G-Swift Relay • Standalone Mode
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setShowGuideModal(false)}
                className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 flex items-center justify-center cursor-pointer"
                aria-label="Tutup"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {isIOS ? (
              <div className="p-3.5 bg-slate-50 rounded-2xl border border-slate-200/80 space-y-2 text-xs text-slate-700 leading-relaxed">
                <p className="font-bold text-slate-900">Langkah di Safari iPhone / iPad:</p>
                <p className="flex items-center gap-2">
                  <span className="w-5 h-5 rounded-full bg-mandiri-blue-600 text-white text-[10px] font-black flex items-center justify-center shrink-0">1</span>
                  <span>Ketuk ikon <strong className="inline-flex items-center gap-0.5"><Share className="w-3.5 h-3.5" /> Bagikan (Share)</strong> di bilah Safari.</span>
                </p>
                <p className="flex items-center gap-2">
                  <span className="w-5 h-5 rounded-full bg-mandiri-blue-600 text-white text-[10px] font-black flex items-center justify-center shrink-0">2</span>
                  <span>Gulir ke bawah dan pilih <strong>Tambahkan ke Layar Utama (Add to Home Screen)</strong>.</span>
                </p>
              </div>
            ) : (
              <div className="p-3.5 bg-slate-50 rounded-2xl border border-slate-200/80 space-y-2.5 text-xs text-slate-700 leading-relaxed">
                <p className="font-bold text-slate-900">
                  {isAndroid ? "Langkah Pasang APK Web di Chrome Android:" : "Langkah Pasang di Chrome / Edge:"}
                </p>
                <p className="flex items-start gap-2">
                  <span className="w-5 h-5 rounded-full bg-mandiri-blue-600 text-white text-[10px] font-black flex items-center justify-center shrink-0 mt-0.5">1</span>
                  <span>
                    Buka aplikasi di browser <strong>Google Chrome Android</strong> (bukan di dalam jendela preview).
                  </span>
                </p>
                <p className="flex items-start gap-2">
                  <span className="w-5 h-5 rounded-full bg-mandiri-blue-600 text-white text-[10px] font-black flex items-center justify-center shrink-0 mt-0.5">2</span>
                  <span>
                    Ketuk menu titik tiga <strong className="inline-flex items-center"><MoreVertical className="w-3.5 h-3.5" /></strong> di pojok kanan atas browser.
                  </span>
                </p>
                <p className="flex items-start gap-2">
                  <span className="w-5 h-5 rounded-full bg-mandiri-blue-600 text-white text-[10px] font-black flex items-center justify-center shrink-0 mt-0.5">3</span>
                  <span>
                    Pilih <strong>&ldquo;Instal aplikasi&rdquo; / &ldquo;Tambahkan ke Layar utama&rdquo;</strong>, lalu ketuk <strong>Instal</strong>.
                  </span>
                </p>
              </div>
            )}

            <button
              type="button"
              onClick={() => setShowGuideModal(false)}
              className="w-full py-2.5 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-bold cursor-pointer transition-colors"
            >
              Mengerti
            </button>
          </div>
        </div>
      )}
    </>
  );
};
