"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";

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

export default function Sidebar() {
  const pathname = usePathname();
  const [lang, setLang] = useState<"EN" | "BN">("EN");

  const links = [
    { href: "/", label: lang === "EN" ? "Dashboard" : "ড্যাশবোর্ড", icon: <IconDashboard /> },
    { href: "/alerts", label: lang === "EN" ? "Alerts & Cases" : "সতর্কতা এবং কেস", icon: <IconAlerts /> },
    { href: "/simulate", label: lang === "EN" ? "Simulator" : "সিমুলেটর", icon: <IconSimulate /> },
  ];

  return (
    <aside className="sidebar border-r border-white/20">
      <div className="px-5 py-6 flex items-center gap-3 border-b border-white/20">
        <div className="text-text-heading"><IconShield /></div>
        <div>
          <div className="text-text-heading font-bold text-[15px] tracking-tight">SuperAgent</div>
          <div className="text-text-muted text-[11px] font-medium">Liquidity & Risk Platform</div>
        </div>
      </div>

      <div className="px-3 pt-6 pb-2">
        <div className="text-[10px] font-semibold uppercase tracking-widest text-text-muted px-3 mb-2">Navigation</div>
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

      {/* Language Toggle */}
      <div className="mx-5 mb-4 p-1 flex bg-white/10 rounded-lg neu-inset">
        <button 
          onClick={() => setLang("EN")}
          className={`flex-1 flex items-center justify-center gap-2 py-2 text-xs font-bold rounded-md transition-all ${lang === "EN" ? "neu-raised text-bkash" : "text-text-muted hover:text-text-heading"}`}
        >
          <IconGlobe /> EN
        </button>
        <button 
          onClick={() => setLang("BN")}
          className={`flex-1 flex items-center justify-center gap-2 py-2 text-xs font-bold rounded-md transition-all ${lang === "BN" ? "neu-raised text-bkash" : "text-text-muted hover:text-text-heading"}`}
        >
          <IconGlobe /> BN
        </button>
      </div>

      <div className="p-4 mx-3 mb-4 rounded-lg neu-inset">
        <div className="flex items-center gap-2 mb-2">
          <div className="live-dot" />
          <span className="text-xs font-semibold text-text-muted">System Status</span>
        </div>
        <div className="text-[11px] text-text-body leading-relaxed">
          {lang === "EN" ? "Backend connected. All feeds operational." : "ব্যাকএন্ড সংযুক্ত। সমস্ত ফিড সচল আছে।"}
        </div>
      </div>
    </aside>
  );
}
