"use client";

import React, { useState, useEffect, useRef } from "react";
import { useAppContext } from "@/context/AppContext";

export interface TransactionColumn {
  id: string;
  translationKey: string;
  width: number;
  minWidth: number;
  maxWidth?: number;
}

const DEFAULT_COLUMNS: TransactionColumn[] = [
  { id: "time", translationKey: "tx.time", width: 140, minWidth: 80, maxWidth: 300 },
  { id: "ref", translationKey: "tx.ref", width: 130, minWidth: 90, maxWidth: 300 },
  { id: "provider", translationKey: "tx.provider", width: 120, minWidth: 90, maxWidth: 240 },
  { id: "type", translationKey: "tx.type", width: 130, minWidth: 100, maxWidth: 280 },
  { id: "account", translationKey: "tx.account", width: 140, minWidth: 100, maxWidth: 300 },
  { id: "amount", translationKey: "tx.amount", width: 130, minWidth: 100, maxWidth: 240 },
  { id: "status", translationKey: "dash.status", width: 120, minWidth: 90, maxWidth: 200 },
];

const LOCAL_STORAGE_KEY = "bnr-baymax-transaction-column-widths";

export default function ResizableTransactionTable({ data }: { data: any[] }) {
  const { t } = useAppContext();
  
  // State for tracking widths
  const [columnWidths, setColumnWidths] = useState<Record<string, number>>(() => {
    // Default to the configuration widths initially to avoid hydration mismatch
    return DEFAULT_COLUMNS.reduce((acc, col) => {
      acc[col.id] = col.width;
      return acc;
    }, {} as Record<string, number>);
  });

  // Client-side only hydration of local storage
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    setIsClient(true);
    try {
      const stored = localStorage.getItem(LOCAL_STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        if (typeof parsed === "object" && parsed !== null) {
          setColumnWidths(prev => ({ ...prev, ...parsed }));
        }
      }
    } catch (e) {
      console.error("Failed to parse column widths from local storage", e);
    }
  }, []);

  // Save to local storage whenever widths change after mounting
  useEffect(() => {
    if (isClient) {
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(columnWidths));
    }
  }, [columnWidths, isClient]);

  // Resizing logic
  const [resizingCol, setResizingCol] = useState<string | null>(null);
  const startXRef = useRef<number>(0);
  const startWidthRef = useRef<number>(0);

  const handlePointerDown = (colId: string, e: React.PointerEvent) => {
    e.preventDefault();
    setResizingCol(colId);
    startXRef.current = e.clientX;
    startWidthRef.current = columnWidths[colId] || 150;
    
    // Disable text selection globally during resize
    document.body.style.userSelect = "none";
    document.body.style.cursor = "col-resize";
  };

  useEffect(() => {
    const handlePointerMove = (e: PointerEvent) => {
      if (!resizingCol) return;
      
      const colDef = DEFAULT_COLUMNS.find(c => c.id === resizingCol);
      if (!colDef) return;

      const diff = e.clientX - startXRef.current;
      let newWidth = startWidthRef.current + diff;
      
      // Clamp widths
      newWidth = Math.max(colDef.minWidth, newWidth);
      if (colDef.maxWidth) {
        newWidth = Math.min(colDef.maxWidth, newWidth);
      }

      setColumnWidths(prev => ({
        ...prev,
        [resizingCol]: newWidth
      }));
    };

    const handlePointerUp = () => {
      if (resizingCol) {
        setResizingCol(null);
        document.body.style.userSelect = "";
        document.body.style.cursor = "";
      }
    };

    if (resizingCol) {
      document.addEventListener("pointermove", handlePointerMove);
      document.addEventListener("pointerup", handlePointerUp);
    }

    return () => {
      document.removeEventListener("pointermove", handlePointerMove);
      document.removeEventListener("pointerup", handlePointerUp);
    };
  }, [resizingCol]);

  const resetColumns = () => {
    const defaults = DEFAULT_COLUMNS.reduce((acc, col) => {
      acc[col.id] = col.width;
      return acc;
    }, {} as Record<string, number>);
    setColumnWidths(defaults);
    localStorage.removeItem(LOCAL_STORAGE_KEY);
  };

  return (
    <div className="neu-raised p-8">
      <div className="flex justify-between items-end mb-4">
        <div></div>
        <button
          onClick={resetColumns}
          className="px-4 py-2 text-xs font-bold rounded-md text-gray-500 hover:text-gray-800 neu-inset transition-colors"
        >
          {t("tx.resetCols")}
        </button>
      </div>

      <div className="overflow-x-auto pb-4 custom-scrollbar">
        <table className="w-full text-sm" style={{ tableLayout: "fixed", minWidth: "800px" }}>
          <colgroup>
            {DEFAULT_COLUMNS.map(col => (
              <col key={col.id} style={{ width: `${columnWidths[col.id]}px` }} />
            ))}
          </colgroup>
          <thead>
            <tr className="border-b border-gray-200 dark:border-gray-700/50 select-none">
              {DEFAULT_COLUMNS.map((col, i) => (
                <th key={col.id} className="relative group text-left py-4 px-4 text-gray-500 font-bold whitespace-nowrap">
                  <div className="flex items-center">
                    <span>{t(col.translationKey)}</span>
                  </div>
                  <div 
                    aria-label={`Resize ${col.translationKey}`}
                    onPointerDown={(e) => handlePointerDown(col.id, e)}
                    className="absolute right-0 top-0 bottom-0 w-4 cursor-col-resize z-10 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center -mr-2 touch-none"
                  >
                    <div className={`w-[3px] h-6 rounded-full ${resizingCol === col.id ? "bg-gray-400 dark:bg-gray-500" : "bg-gray-300 dark:bg-gray-600"} transition-colors`} />
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.map((tx: any, i: number) => {
              const isOut = tx.type === "cash_out";
              return (
                <tr key={tx.id} className={`hover:bg-gray-50 dark:hover:bg-gray-800/20 transition-colors ${i < data.length - 1 ? "border-b border-gray-100 dark:border-gray-800/50" : ""}`}>
                  <td className="py-4 px-4 text-gray-800 font-mono font-bold text-xs truncate" title={tx.time}>{tx.time}</td>
                  <td className="py-4 px-4 text-gray-500 font-mono text-xs truncate" title={tx.ref}>{tx.ref || `TXN1000${tx.id}`}</td>
                  <td className="py-4 px-4 truncate">
                    <span className={`badge badge-${tx.provider.toLowerCase()}`}>{tx.provider}</span>
                  </td>
                  <td className="py-4 px-4 text-gray-700 font-bold capitalize truncate" title={tx.type.replace("_", " ")}>{tx.type.replace("_", " ")}</td>
                  <td className="py-4 px-4 text-gray-500 font-mono text-xs truncate" title={tx.account}>{tx.account}</td>
                  <td className={`py-4 px-4 text-left font-mono font-bold truncate ${isOut ? "text-red-500" : "text-green-600"}`} title={isOut ? "-" + tx.amount : "+" + tx.amount}>
                    {isOut ? "-" : "+"}৳{tx.amount.toLocaleString("en-BD")}
                  </td>
                  <td className="py-4 px-4 text-left truncate">
                    <span className="badge badge-success">{t("tx.completed")}</span>
                  </td>
                </tr>
              );
            })}
            {data.length === 0 && (
              <tr>
                <td colSpan={7} className="text-center py-8 text-gray-500">
                  No transactions found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
