"use client";

import { useState } from "react";
import { askAi } from "@/lib/api/ai";
import { DEMO_AGENT_CODE } from "@/context/SimulationContext";
import { Bot, X, Send, Loader2 } from "lucide-react";

export default function AIChatPanel() {
  const [isOpen, setIsOpen] = useState(false);
  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState<{ role: "user" | "ai", text: string }[]>([
    { role: "ai", text: "Hello! I am Baymax. Ask me anything about your current operational state." }
  ]);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!question.trim()) return;

    const userQ = question.trim();
    setMessages(prev => [...prev, { role: "user", text: userQ }]);
    setQuestion("");
    setIsLoading(true);

    try {
      const res = await askAi({ agentCode: DEMO_AGENT_CODE, question: userQ });
      setMessages(prev => [...prev, { role: "ai", text: res.answer }]);
    } catch (err: any) {
      setMessages(prev => [...prev, { role: "ai", text: `Error: ${err.message}` }]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      {/* Floating Button */}
      <button
        onClick={() => setIsOpen(true)}
        className={`fixed bottom-6 right-6 p-4 rounded-full bg-blue-600 text-white shadow-lg hover:bg-blue-700 transition-all z-50 flex items-center justify-center ${isOpen ? 'scale-0' : 'scale-100'}`}
      >
        <Bot size={24} />
      </button>

      {/* Chat Panel */}
      <div 
        className={`fixed bottom-6 right-6 w-96 max-w-[calc(100vw-32px)] bg-white rounded-2xl shadow-2xl border border-gray-100 flex flex-col z-50 overflow-hidden transition-all transform origin-bottom-right duration-300 ${isOpen ? 'scale-100 opacity-100' : 'scale-0 opacity-0 pointer-events-none'}`}
        style={{ height: "500px", maxHeight: "calc(100vh - 100px)" }}
      >
        {/* Header */}
        <div className="bg-blue-600 p-4 flex items-center justify-between text-white">
          <div className="flex items-center gap-2 font-bold">
            <Bot size={20} /> Baymax AI Assistant
          </div>
          <button onClick={() => setIsOpen(false)} className="hover:bg-blue-700 p-1 rounded-md transition-colors">
            <X size={20} />
          </button>
        </div>

        {/* Messages */}
        <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-4 bg-gray-50/50">
          {messages.map((msg, i) => (
            <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
              <div className={`px-4 py-2 rounded-2xl max-w-[85%] text-sm ${msg.role === 'user' ? 'bg-blue-600 text-white rounded-br-sm' : 'bg-white border border-gray-100 text-gray-800 shadow-sm rounded-bl-sm whitespace-pre-wrap'}`}>
                {msg.text}
              </div>
            </div>
          ))}
          {isLoading && (
            <div className="flex justify-start">
              <div className="px-4 py-2 bg-white border border-gray-100 text-gray-500 rounded-2xl rounded-bl-sm flex items-center gap-2 shadow-sm">
                <Loader2 size={16} className="animate-spin" /> Baymax is thinking...
              </div>
            </div>
          )}
        </div>

        {/* Input */}
        <div className="p-3 bg-white border-t border-gray-100">
          <form onSubmit={handleSubmit} className="flex gap-2 items-center">
            <input
              type="text"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              placeholder="Ask about your risk..."
              className="flex-1 px-4 py-2 border border-gray-200 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 text-sm"
              disabled={isLoading}
            />
            <button 
              type="submit"
              disabled={isLoading || !question.trim()}
              className="p-2 rounded-full bg-blue-600 text-white disabled:bg-gray-300 disabled:text-white transition-colors"
            >
              <Send size={18} className={question.trim() ? "translate-x-[1px]" : ""} />
            </button>
          </form>
        </div>
      </div>
    </>
  );
}
