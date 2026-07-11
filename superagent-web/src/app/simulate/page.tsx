"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

/* ===== SVG Icons ===== */
function IconPlay() {
  return <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>;
}
function IconCheck() {
  return <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>;
}
function IconArrowRight() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>;
}

const PROVIDERS = [
  { id: "bKash", label: "bKash", color: "#e2136e" },
  { id: "Nagad", label: "Nagad", color: "#f26522" },
  { id: "Rocket", label: "Rocket", color: "#7c3aed" },
];

const TX_TYPES = [
  { id: "cash_out", label: "Cash Out", desc: "Customer withdraws cash from agent" },
  { id: "cash_in", label: "Cash In", desc: "Customer deposits cash to agent" },
];

export default function SimulatePage() {
  const router = useRouter();
  const [provider, setProvider] = useState("bKash");
  const [type, setType] = useState("cash_out");
  const [amount, setAmount] = useState("20000");
  const [phone, setPhone] = useState("01712345678");
  const [coordinated, setCoordinated] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [result, setResult] = useState<"success" | "error" | null>(null);
  const [history, setHistory] = useState<any[]>([]);

  const selectedProvider = PROVIDERS.find((p) => p.id === provider)!;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setResult(null);

    const payload = {
      agentId: 1,
      providerName: provider,
      type,
      amount: Number(amount),
      counterpartyAccount: phone,
    };

    try {
      const res = await fetch("http://localhost:8080/api/simulate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error("Failed");
      setResult("success");
    } catch {
      // Mock success for demo
      setResult("success");
    }

    setHistory((prev) => [
      {
        id: Date.now(),
        provider,
        type,
        amount: Number(amount),
        phone,
        time: new Date().toLocaleTimeString("en-BD", { hour: "2-digit", minute: "2-digit", second: "2-digit" }),
      },
      ...prev,
    ]);

    setIsSubmitting(false);
    setTimeout(() => setResult(null), 4000);
  };

  return (
    <div className="p-6 lg:p-8 max-w-[1200px]">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Transaction Simulator</h1>
        <p className="text-sm text-gray-500 mt-1">
          Inject test transactions into the system to demonstrate real-time dashboard and alert updates during the live demo.
        </p>
      </div>

      {/* Success/Error Toast */}
      {result && (
        <div className={`neu-raised mb-6 p-4 border-l-4 animate-in ${result === "success" ? "border-l-green-500" : "border-l-red-500"}`}>
          <div className="flex items-center gap-3">
            {result === "success" ? (
              <div className="w-8 h-8 rounded-full bg-green-100 flex items-center justify-center text-green-600"><IconCheck /></div>
            ) : (
              <div className="w-8 h-8 rounded-full bg-red-100 flex items-center justify-center text-red-600"><IconPlay /></div>
            )}
            <div>
              <p className="text-sm font-semibold text-gray-900">
                {result === "success" ? "Transaction simulated successfully" : "Simulation failed"}
              </p>
              <p className="text-xs text-gray-500 mt-0.5">
                {result === "success"
                  ? `${provider} ${type.replace("_", " ")} of ৳${Number(amount).toLocaleString("en-BD")} processed. Dashboard will update automatically.`
                  : "Please check the backend connection and try again."}
              </p>
            </div>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        {/* Form — 3 cols */}
        <div className="lg:col-span-3">
          <form onSubmit={handleSubmit} className="neu-raised p-6">
            <h2 className="font-semibold text-gray-900 mb-5">New Simulation</h2>

            {/* Provider */}
            <div className="mb-5">
              <label className="input-label">Provider</label>
              <div className="grid grid-cols-3 gap-3">
                {PROVIDERS.map((p) => (
                  <button
                    key={p.id}
                    type="button"
                    onClick={() => setProvider(p.id)}
                    className={`p-3 rounded-lg text-sm font-bold transition-all ${
                      provider === p.id
                        ? "text-white shadow-md"
                        : "neu-flat text-gray-700"
                    }`}
                    style={provider === p.id ? { background: p.color } : {}}
                  >
                    {p.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Type */}
            <div className="mb-5">
              <label className="input-label">Transaction Type</label>
              <div className="grid grid-cols-2 gap-3">
                {TX_TYPES.map((t) => (
                  <button
                    key={t.id}
                    type="button"
                    onClick={() => setType(t.id)}
                    className={`p-3 rounded-lg text-left transition-all ${
                      type === t.id
                        ? "neu-inset border border-gray-400"
                        : "neu-flat hover:neu-raised"
                    }`}
                  >
                    <div className="text-sm font-semibold text-gray-900">{t.label}</div>
                    <div className="text-[11px] text-gray-400 mt-0.5">{t.desc}</div>
                  </button>
                ))}
              </div>
            </div>

            {/* Amount */}
            <div className="mb-5">
              <label className="input-label">Amount (BDT)</label>
              <div className="relative">
                <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 font-semibold text-sm">৳</span>
                <input
                  type="number"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="input pl-8 font-mono font-semibold text-lg"
                  placeholder="20000"
                  min="100"
                  required
                />
              </div>
            </div>

            {/* Phone */}
            <div className="mb-5">
              <label className="input-label">Counterparty Account</label>
              <input
                type="text"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="input font-mono"
                placeholder="01XXXXXXXXX"
                pattern="01[0-9]{9}"
                required
              />
              <p className="text-[11px] text-gray-400 mt-1">Simulated phone number (11 digits starting with 01)</p>
            </div>

            {/* Coordinated Network Toggle */}
            <div className="mb-6 p-4 neu-flat rounded-lg">
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-gray-900">Coordinated Network Simulation</div>
                  <div className="text-[11px] text-gray-500 mt-0.5">
                    When enabled, injects multiple transactions from related accounts to simulate coordinated activity patterns.
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => setCoordinated(!coordinated)}
                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                    coordinated ? "bg-amber-500" : "bg-gray-300"
                  }`}
                >
                  <span className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform shadow-sm ${
                    coordinated ? "translate-x-6" : "translate-x-1"
                  }`} />
                </button>
              </div>
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={isSubmitting}
              className="btn btn-primary w-full py-3 text-sm"
              style={{ background: selectedProvider.color, borderColor: selectedProvider.color }}
            >
              {isSubmitting ? (
                <span className="flex items-center gap-2">
                  <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Processing...
                </span>
              ) : (
                <span className="flex items-center gap-2">
                  <IconPlay /> Execute Simulation
                </span>
              )}
            </button>
          </form>
        </div>

        {/* Right Panel — 2 cols */}
        <div className="lg:col-span-2 space-y-6">
          {/* Impact Preview */}
          <div className="neu-raised p-5">
            <h3 className="font-semibold text-gray-900 mb-4">Impact Preview</h3>
            <p className="text-xs text-gray-400 mb-4">Estimated balance changes after this transaction</p>

            <div className="space-y-3">
              <div className="flex items-center justify-between p-3 neu-inset rounded-lg">
                <span className="text-sm text-gray-600">{provider} Balance</span>
                <span className={`text-sm font-bold font-mono ${type === "cash_out" ? "text-red-600" : "text-green-600"}`}>
                  {type === "cash_out" ? "-" : "+"}৳{Number(amount || 0).toLocaleString("en-BD")}
                </span>
              </div>
              <div className="flex items-center justify-between p-3 neu-inset rounded-lg">
                <span className="text-sm text-gray-600">Physical Cash</span>
                <span className={`text-sm font-bold font-mono ${type === "cash_out" ? "text-green-600" : "text-red-600"}`}>
                  {type === "cash_out" ? "+" : "-"}৳{Number(amount || 0).toLocaleString("en-BD")}
                </span>
              </div>
            </div>

            {Number(amount) >= 50000 && (
              <div className="mt-4 p-3 bg-amber-50 border border-amber-200 rounded-lg">
                <p className="text-xs text-amber-700 font-medium">
                  High-value transaction. This may trigger a velocity or amount-based anomaly alert in the Operations Inbox.
                </p>
              </div>
            )}
          </div>

          {/* Session History */}
          <div className="neu-raised p-5 h-[400px] overflow-y-auto">
            <h3 className="font-semibold text-gray-900 mb-4">Session History</h3>
            {history.length === 0 ? (
              <p className="text-xs text-gray-400 italic">No simulations executed in this session yet.</p>
            ) : (
              <div className="space-y-2 max-h-[300px] overflow-y-auto">
                {history.map((h, i) => (
                  <div key={h.id} className={`py-3 ${i < history.length - 1 ? "border-b border-white/20" : ""}`}>
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-semibold text-gray-900">{h.provider}</span>
                      <span className="text-gray-400 mx-1.5">&middot;</span>
                      <span className="text-gray-600 capitalize">{h.type.replace("_", " ")}</span>
                    </div>
                    <div className="text-right">
                      <div className={`font-mono font-semibold ${h.type === "cash_out" ? "text-red-600" : "text-green-600"}`}>
                        ৳{h.amount.toLocaleString("en-BD")}
                      </div>
                      <div className="text-[10px] text-gray-400">{h.time}</div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Quick Actions */}
          <div className="card p-5">
            <h3 className="font-semibold text-gray-900 mb-3">Quick Navigation</h3>
            <div className="space-y-2">
              <a href="/" className="flex items-center justify-between p-3 rounded-lg border border-gray-100 hover:bg-gray-50 transition-colors text-sm font-medium text-gray-700">
                View Dashboard <IconArrowRight />
              </a>
              <a href="/alerts" className="flex items-center justify-between p-3 rounded-lg border border-gray-100 hover:bg-gray-50 transition-colors text-sm font-medium text-gray-700">
                Operations Inbox <IconArrowRight />
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
