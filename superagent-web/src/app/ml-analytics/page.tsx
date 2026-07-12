"use client";

import React, { useState } from "react";
import { 
  Activity, 
  AlertTriangle, 
  BrainCircuit, 
  Target, 
  TrendingUp,
  Search,
  ExternalLink,
  Loader2
} from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";

export default function MLAnalyticsPage() {
  const [activeTab, setActiveTab] = useState("overview");
  const [isCreating, setIsCreating] = useState<string | null>(null);
  const router = useRouter();

  const handleCreateCase = async (tx: any) => {
    setIsCreating(tx.id);
    try {
      // POST to the backend manual case endpoint
      await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'}/api/v1/cases/manual`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          agentCode: "SA-DHAKA-001",
          providerCode: tx.provider,
          priority: tx.risk === 'HIGH' ? 'CRITICAL' : tx.risk === 'MEDIUM' ? 'HIGH' : 'MEDIUM',
          title: `ML Flag: ${tx.feature} detected in volume ${tx.amount}`,
          description: `The ML Random Forest model flagged transaction window ${tx.id} due to ${tx.feature}. System requires human compliance review to ensure no AML policy violation.`,
          recommendedNextStep: "Review the transaction logs and contact the agent for verification.",
          createdBy: "ML Anomaly Detector"
        })
      });
      router.push('/cases');
    } catch (e) {
      console.error(e);
      setIsCreating(null);
    }
  };

  // Mocked ML Metrics from the Python Model output
  const mlMetrics = {
    precision: 1.0,
    recall: 1.0,
    f1: 1.0,
    threshold: 0.20,
    confusion_matrix: {
      tn: 987,
      fp: 0,
      fn: 0,
      tp: 55
    }
  };

  // Mocked recently flagged transactions for the UI
  const flaggedTransactions = [
    { id: "TXN-9021-BK", time: "10:15 AM", provider: "BKASH", amount: "৳ 450,000", feature: "Cash-Out Velocity Spike", risk: "HIGH", status: "Requires Review" },
    { id: "TXN-8812-NG", time: "09:42 AM", provider: "NAGAD", amount: "৳ 120,000", feature: "Repeated Amount Cluster", risk: "MEDIUM", status: "Requires Review" },
    { id: "TXN-8110-BK", time: "08:30 AM", provider: "BKASH", amount: "৳ 890,000", feature: "Baseline Deviation (+400%)", risk: "HIGH", status: "Requires Review" },
    { id: "TXN-7944-RC", time: "08:15 AM", provider: "ROCKET", amount: "৳ 60,000", feature: "Event Demand Spike", risk: "LOW", status: "Requires Review" }
  ];

  return (
    <div className="animate-in pb-12">
      <div className="flex justify-between items-end mb-6">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <BrainCircuit className="text-purple-500" size={24} />
            <h1 className="text-2xl font-bold text-text-heading tracking-tight">Machine Learning Risk Analytics</h1>
          </div>
          <p className="text-text-muted text-sm font-medium">Event-Aware Review Signal Detection Model</p>
        </div>
        <div className="flex gap-2">
          <div className="badge badge-success px-3 py-1 text-xs">MODEL: ACTIVE</div>
          <div className="badge badge-info px-3 py-1 text-xs">V 1.0.0 (RANDOM FOREST)</div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-6 border-b border-white/20 pb-2">
        <button 
          onClick={() => setActiveTab("overview")}
          className={`px-4 py-2 text-sm font-bold rounded-t-lg transition-colors ${activeTab === 'overview' ? 'text-purple-600 border-b-2 border-purple-600' : 'text-text-muted hover:text-text-heading'}`}
        >
          Model Performance Overview
        </button>
        <button 
          onClick={() => setActiveTab("flagged")}
          className={`px-4 py-2 text-sm font-bold rounded-t-lg transition-colors ${activeTab === 'flagged' ? 'text-purple-600 border-b-2 border-purple-600' : 'text-text-muted hover:text-text-heading'}`}
        >
          Recently Flagged Activity
        </button>
      </div>

      {activeTab === "overview" && (
        <div className="space-y-6">
          {/* Metrics Grid */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="neu-raised p-5">
              <div className="flex items-center gap-2 mb-3 text-text-muted">
                <Target size={16} />
                <span className="metric-label">Precision</span>
              </div>
              <div className="metric-value text-green-600">{(mlMetrics.precision * 100).toFixed(0)}%</div>
              <p className="text-xs text-text-muted mt-2">Zero False Positives</p>
            </div>
            
            <div className="neu-raised p-5">
              <div className="flex items-center gap-2 mb-3 text-text-muted">
                <Activity size={16} />
                <span className="metric-label">Recall</span>
              </div>
              <div className="metric-value text-green-600">{(mlMetrics.recall * 100).toFixed(0)}%</div>
              <p className="text-xs text-text-muted mt-2">Zero False Negatives</p>
            </div>

            <div className="neu-raised p-5">
              <div className="flex items-center gap-2 mb-3 text-text-muted">
                <TrendingUp size={16} />
                <span className="metric-label">F1 Score</span>
              </div>
              <div className="metric-value text-green-600">{(mlMetrics.f1 * 100).toFixed(0)}%</div>
              <p className="text-xs text-text-muted mt-2">Perfect Balance</p>
            </div>

            <div className="neu-raised p-5">
              <div className="flex items-center gap-2 mb-3 text-text-muted">
                <AlertTriangle size={16} />
                <span className="metric-label">Optimum Threshold</span>
              </div>
              <div className="metric-value text-purple-600">{mlMetrics.threshold.toFixed(2)}</div>
              <p className="text-xs text-text-muted mt-2">Decision Boundary</p>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Confusion Matrix */}
            <div className="neu-raised p-6">
              <h3 className="font-bold text-text-heading mb-4 flex items-center gap-2">
                <Search size={18} /> Confusion Matrix (Test Split)
              </h3>
              <div className="grid grid-cols-2 gap-4">
                <div className="neu-inset p-4 flex flex-col items-center justify-center bg-green-500/10">
                  <span className="text-xs text-text-muted uppercase font-bold mb-1">True Negative</span>
                  <span className="text-3xl font-bold text-green-600">{mlMetrics.confusion_matrix.tn}</span>
                  <span className="text-[10px] text-text-muted mt-1 text-center">Correctly ignored normal behavior</span>
                </div>
                <div className="neu-inset p-4 flex flex-col items-center justify-center bg-red-500/10">
                  <span className="text-xs text-text-muted uppercase font-bold mb-1">False Positive</span>
                  <span className="text-3xl font-bold text-red-500">{mlMetrics.confusion_matrix.fp}</span>
                  <span className="text-[10px] text-text-muted mt-1 text-center">Incorrectly flagged (0 is perfect)</span>
                </div>
                <div className="neu-inset p-4 flex flex-col items-center justify-center bg-red-500/10">
                  <span className="text-xs text-text-muted uppercase font-bold mb-1">False Negative</span>
                  <span className="text-3xl font-bold text-red-500">{mlMetrics.confusion_matrix.fn}</span>
                  <span className="text-[10px] text-text-muted mt-1 text-center">Missed anomalies (0 is perfect)</span>
                </div>
                <div className="neu-inset p-4 flex flex-col items-center justify-center bg-purple-500/10">
                  <span className="text-xs text-text-muted uppercase font-bold mb-1">True Positive</span>
                  <span className="text-3xl font-bold text-purple-600">{mlMetrics.confusion_matrix.tp}</span>
                  <span className="text-[10px] text-text-muted mt-1 text-center">Correctly flagged for review</span>
                </div>
              </div>
            </div>

            {/* Model Info */}
            <div className="neu-raised p-6">
              <h3 className="font-bold text-text-heading mb-4 flex items-center gap-2">
                <BrainCircuit size={18} /> Architecture Details
              </h3>
              <div className="space-y-4">
                <div className="flex justify-between items-center border-b border-white/10 pb-2">
                  <span className="text-sm font-medium text-text-muted">Algorithm</span>
                  <span className="text-sm font-bold text-text-heading">RandomForestClassifier</span>
                </div>
                <div className="flex justify-between items-center border-b border-white/10 pb-2">
                  <span className="text-sm font-medium text-text-muted">Behavioral Window</span>
                  <span className="text-sm font-bold text-text-heading">15 Minutes</span>
                </div>
                <div className="flex justify-between items-center border-b border-white/10 pb-2">
                  <span className="text-sm font-medium text-text-muted">Target Variable</span>
                  <span className="text-sm font-bold text-text-heading">requires_review</span>
                </div>
                <div className="flex justify-between items-center border-b border-white/10 pb-2">
                  <span className="text-sm font-medium text-text-muted">Key Feature Used</span>
                  <span className="text-sm font-bold text-text-heading">baseline_deviation_cash_out</span>
                </div>
                <div className="mt-4 p-3 neu-inset bg-blue-500/5 text-xs text-text-body rounded-lg leading-relaxed">
                  <strong>How it works:</strong> The ML model operates strictly offline. It analyzes 15-minute sliding windows of synthetic transaction volumes against historical baselines. When a massive deviation or cluster is detected, it flags the window as `requires_review = 1` for a human compliance officer.
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === "flagged" && (
        <div className="neu-raised overflow-hidden">
          <div className="p-4 border-b border-white/20 bg-gray-50/50 dark:bg-slate-800/50 flex justify-between items-center">
            <h3 className="font-bold text-text-heading flex items-center gap-2">
              <AlertTriangle size={18} className="text-warning" /> Flagged Activity Log (Simulated)
            </h3>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-white/5 dark:bg-slate-900/50">
                  <th className="p-4 table-header">Time</th>
                  <th className="p-4 table-header">Transaction Group</th>
                  <th className="p-4 table-header">Provider</th>
                  <th className="p-4 table-header">Volume</th>
                  <th className="p-4 table-header">Anomaly Feature Detected</th>
                  <th className="p-4 table-header">Risk Level</th>
                  <th className="p-4 table-header">Action</th>
                </tr>
              </thead>
              <tbody>
                {flaggedTransactions.map((tx, idx) => (
                  <tr key={idx} className="table-row">
                    <td className="p-4 text-sm font-medium text-text-muted">{tx.time}</td>
                    <td className="p-4 text-sm font-bold text-text-heading">{tx.id}</td>
                    <td className="p-4">
                      <span className={`badge ${tx.provider === 'BKASH' ? 'badge-bkash' : tx.provider === 'NAGAD' ? 'badge-nagad' : 'badge-rocket'}`}>
                        {tx.provider}
                      </span>
                    </td>
                    <td className="p-4 text-sm font-bold text-text-heading">{tx.amount}</td>
                    <td className="p-4 text-sm text-text-body flex items-center gap-2">
                      <TrendingUp size={14} className="text-purple-500" />
                      {tx.feature}
                    </td>
                    <td className="p-4">
                      <span className={`badge ${tx.risk === 'HIGH' ? 'badge-critical' : tx.risk === 'MEDIUM' ? 'badge-warning' : 'badge-info'}`}>
                        {tx.risk}
                      </span>
                    </td>
                    <td className="p-4">
                      <button 
                        onClick={() => handleCreateCase(tx)}
                        disabled={isCreating === tx.id}
                        className="btn btn-sm btn-ghost border border-white/20 text-xs flex items-center gap-1 hover:text-purple-500 hover:border-purple-500 transition-colors disabled:opacity-50"
                      >
                        {isCreating === tx.id ? <Loader2 size={12} className="animate-spin" /> : "Create Case"} 
                        {isCreating !== tx.id && <ExternalLink size={12} />}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="p-4 border-t border-white/20 text-xs text-center text-text-muted">
            Displaying the most recent 15-minute behavioral windows flagged by the Random Forest classifier.
          </div>
        </div>
      )}

    </div>
  );
}
