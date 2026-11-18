import React from "react";
import { WifiOff } from "lucide-react";
import { useOnlineStatus } from "../hooks/useOnlineStatus";

export const OfflineIndicator: React.FC = () => {
  const isOnline = useOnlineStatus();

  if (isOnline) return null;

  return (
    <div className="fixed bottom-20 left-4 z-[125] flex items-center gap-2 rounded-full bg-amber-500 px-3.5 py-1.5 text-[11px] font-bold text-white shadow-lg border border-amber-400">
      <WifiOff className="w-3.5 h-3.5 shrink-0" />
      <span>Mode Offline — Menggunakan cache aplikasi</span>
    </div>
  );
};
