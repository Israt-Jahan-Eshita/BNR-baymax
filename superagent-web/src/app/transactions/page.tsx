"use client";

import { useState } from "react";
import { useAppContext } from "@/context/AppContext";
import { useSimulation } from "@/context/SimulationContext";
import ResizableTransactionTable from "@/components/transactions/ResizableTransactionTable";

export default function Transactions() {
  const { t } = useAppContext();
  const { transactions } = useSimulation();
  
  const [filterProvider, setFilterProvider] = useState<string | "All">("All");

  const filteredData = filterProvider === "All" 
    ? transactions 
    : transactions.filter((tx: any) => tx.provider === filterProvider);

  return (
    <div className="p-6 lg:p-8 max-w-[1600px] mx-auto">
      {/* === TOP BAR === */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 tracking-tight">{t("tx.title")}</h1>
          <p className="text-sm text-gray-500 mt-1">{t("tx.subtitle")}</p>
        </div>
        
        <div className="flex gap-2 p-1 neu-inset rounded-lg">
          {["All", "bKash", "Nagad", "Rocket"].map(p => (
            <button
              key={p}
              onClick={() => setFilterProvider(p)}
              className={`px-4 py-2 text-xs font-bold rounded-md transition-all ${filterProvider === p ? "neu-raised text-gray-900" : "text-gray-500 hover:text-gray-800"}`}
            >
              {p === "All" ? t("tx.all") : p}
            </button>
          ))}
        </div>
      </div>

      <ResizableTransactionTable data={filteredData} />
    </div>
  );
}
