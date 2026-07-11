"use client";

import { useState, useEffect } from "react";
import { useAppContext } from "@/context/AppContext";
import { DEMO_AGENT_CODE, useSimulation } from "@/context/SimulationContext";
import ResizableTransactionTable from "@/components/transactions/ResizableTransactionTable";
import { getTransactions, TransactionSummaryResponse } from "@/lib/api/transactions";

export default function Transactions() {
  const { t } = useAppContext();
  const { refreshCounter } = useSimulation();
  
  const [filterProvider, setFilterProvider] = useState<string | "All">("All");
  const [transactions, setTransactions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadTransactions() {
      try {
        setLoading(true);
        const res = await getTransactions(DEMO_AGENT_CODE, {
          providerCode: filterProvider === "All" ? undefined : filterProvider,
          size: 50
        });
        
        // Map backend DTO to frontend table format
        const mapped = res.transactions.map((tx: TransactionSummaryResponse) => ({
          id: tx.transactionReference,
          provider: tx.providerCode,
          type: tx.type.toLowerCase(),
          amount: tx.amount,
          account: tx.syntheticAccountId,
          time: new Date(tx.occurredAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit', second:'2-digit'}),
          timestampMs: new Date(tx.occurredAt).getTime()
        }));
        setTransactions(mapped);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    loadTransactions();
  }, [filterProvider, refreshCounter]);

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

      {loading ? (
        <div className="p-12 text-center text-gray-500 neu-inset rounded-xl font-bold">Loading recent transactions...</div>
      ) : (
        <ResizableTransactionTable data={transactions} />
      )}
    </div>
  );
}
