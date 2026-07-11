"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAppContext } from "@/context/AppContext";
import { Language } from "@/lib/translations";

function IconDashboard() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="3" width="7" height="7" rx="1" /><rect x="14" y="3" width="7" height="7" rx="1" /><rect x="3" y="14" width="7" height="7" rx="1" /><rect x="14" y="14" width="7" height="7" rx="1" />
    </svg>
  );
}

function IconAlerts() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.73 21a2 2 0 0 1-3.46 0" />
    </svg>
  );
}

function IconSimulate() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polygon points="5 3 19 12 5 21 5 3" />
    </svg>
  );
}

function IconShield() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
    </svg>
  );
}

function IconGlobe() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10" />
      <line x1="2" y1="12" x2="22" y2="12" />
      <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
    </svg>
  );
}

function IconTransactions() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
    </svg>
  );
}

function IconMoon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
    </svg>
  );
}

function IconSun() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="5"></circle>
      <line x1="12" y1="1" x2="12" y2="3"></line>
      <line x1="12" y1="21" x2="12" y2="23"></line>
      <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
      <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
      <line x1="1" y1="12" x2="3" y2="12"></line>
      <line x1="21" y1="12" x2="23" y2="12"></line>
      <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
      <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
    </svg>
  );
}

function IconMetrics() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
    </svg>
  );
}

export default function Sidebar() {
  const pathname = usePathname();
  const { theme, toggleTheme, language, setLanguage, t } = useAppContext();

  const links = [
    { href: "/", label: t("nav.dashboard"), icon: <IconDashboard /> },
    { href: "/transactions", label: t("nav.recentTx"), icon: <IconTransactions /> },
    { href: "/alerts", label: t("nav.alerts"), icon: <IconAlerts /> },
    { href: "/simulate", label: t("nav.simulator"), icon: <IconSimulate /> },
    { href: "/validation", label: t("nav.validation"), icon: <IconMetrics /> },
  ];

  return (
    <aside className="sidebar border-r border-white/20">
      <div className="px-5 py-6 flex items-center gap-3 border-b border-white/20">
        <div className="text-text-heading"><IconShield /></div>
        <div>
          <div className="text-text-heading font-bold text-[15px] tracking-tight">{t("nav.title")}</div>
          <div className="text-text-muted text-[11px] font-medium">{t("nav.subtitle")}</div>
        </div>
      </div>

      <div className="px-3 pt-6 pb-2">
        <div className="text-[10px] font-semibold uppercase tracking-widest text-text-muted px-3 mb-2">{t("nav.navigation")}</div>
      </div>
      <nav className="flex-1">
        {links.map((link) => (
          <Link
            key={link.href}
            href={link.href}
            className={`sidebar-link ${pathname === link.href ? "sidebar-link-active" : ""}`}
          >
            {link.icon}
            {link.label}
          </Link>
        ))}
      </nav>

      {/* Theme Toggle */}
      <div className="px-5 mb-4">
        <button 
          onClick={toggleTheme}
          className="w-full flex items-center justify-center gap-2 py-2 text-xs font-bold rounded-lg neu-flat text-text-muted hover:text-text-heading transition-all"
        >
          {theme === "dark" ? <><IconSun /> Light Mode</> : <><IconMoon /> Dark Mode</>}
        </button>
      </div>

      {/* Language Toggle (3-way) */}
      <div className="mx-5 mb-4 p-1 flex bg-white/10 rounded-lg neu-inset">
        {(["EN", "BL", "BN"] as Language[]).map((l) => (
          <button 
            key={l}
            onClick={() => setLanguage(l)}
            className={`flex-1 flex items-center justify-center py-2 text-[11px] font-bold rounded-md transition-all ${language === l ? "neu-raised text-bkash" : "text-text-muted hover:text-text-heading"}`}
          >
            {l}
          </button>
        ))}
      </div>

      <div className="p-4 mx-3 mb-4 rounded-lg neu-inset">
        <div className="flex items-center gap-2 mb-2">
          <div className="live-dot" />
          <span className="text-xs font-semibold text-text-muted">System Status</span>
        </div>
        <div className="text-[11px] text-text-body leading-relaxed">
          {language === "BN" ? "ব্যাকএন্ড সংযুক্ত। সমস্ত ফিড সচল।" : 
           language === "BL" ? "Backend connected. Shob feed shochol." : 
           "Backend connected. All feeds operational."}
        </div>
      </div>
    </aside>
  );
}
