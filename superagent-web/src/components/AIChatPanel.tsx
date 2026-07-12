"use client";

import { useState, useRef } from "react";
import { analyzeWithBaymax, transcribeAudio, generateSpeechUrl, BaymaxResponse } from "@/lib/api/ai";
import { DEMO_AGENT_CODE } from "@/context/SimulationContext";
import { Bot, X, Send, Loader2, Mic, Square, Volume2, ShieldAlert, CheckCircle2, TrendingUp, ChevronDown, ChevronUp } from "lucide-react";

type Message = {
  id: string;
  role: "user" | "ai";
  text: string;
  structuredData?: BaymaxResponse;
  audioUrl?: string;
  isPlaying?: boolean;
};

function StructuredDataDisplay({ data }: Readonly<{ data: BaymaxResponse }>) {
  const [expanded, setExpanded] = useState(false);
  
  if (!data.evidenceList?.length && !data.reasoningSteps?.length && !data.whatIfProjections?.length && !data.actionItems?.length) {
    return null;
  }

  return (
    <div className="mt-3 text-xs bg-gray-50 dark:bg-slate-800 border border-gray-100 dark:border-slate-600 rounded-lg overflow-hidden">
      <button 
        onClick={() => setExpanded(!expanded)} 
        className="w-full flex items-center justify-between p-2 font-medium text-gray-700 dark:text-gray-200 bg-gray-100/50 dark:bg-slate-700/50 hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
      >
        <div className="flex items-center gap-2">
          {data.confidence === 'HIGH' && <ShieldAlert size={14} className="text-green-600" />}
          {data.confidence === 'MEDIUM' && <ShieldAlert size={14} className="text-amber-500" />}
          {data.confidence !== 'HIGH' && data.confidence !== 'MEDIUM' && <ShieldAlert size={14} className="text-red-500" />}
          Confidence: {data.confidence}
        </div>
        {expanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
      </button>
      
      {expanded && (
        <div className="p-3 flex flex-col gap-3">
          {data.reasoningSteps?.length > 0 && (
            <div>
              <strong className="text-purple-600 flex items-center gap-1 mb-1"><Bot size={12}/> Reasoning Steps</strong>
              <ul className="list-disc pl-4 text-gray-600 dark:text-gray-300 space-y-1">
                {data.reasoningSteps.map((a) => <li key={`reasoning-${a}`}>{a}</li>)}
              </ul>
            </div>
          )}
          {data.whatIfProjections?.length > 0 && (
            <div>
              <strong className="text-blue-600 flex items-center gap-1 mb-1"><TrendingUp size={12}/> Projections</strong>
              <ul className="list-disc pl-4 text-gray-600 dark:text-gray-300 space-y-1">
                {data.whatIfProjections.map((p) => <li key={`proj-${p}`}>{p}</li>)}
              </ul>
            </div>
          )}
          {data.evidenceList?.length > 0 && (
            <div>
              <strong className="text-gray-700 flex items-center gap-1 mb-1"><ShieldAlert size={12}/> Evidence</strong>
              <ul className="list-disc pl-4 text-gray-600 dark:text-gray-300 space-y-1">
                {data.evidenceList.map((e) => <li key={`evi-${e}`}>{e}</li>)}
              </ul>
            </div>
          )}
          {data.actionItems?.length > 0 && (
            <div>
              <strong className="text-green-600 flex items-center gap-1 mb-1"><CheckCircle2 size={12}/> Action Items</strong>
              <ul className="list-disc pl-4 text-gray-600 dark:text-gray-300 space-y-1">
                {data.actionItems.map((r) => <li key={`rec-${r}`}>{r}</li>)}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default function AIChatPanel() {
  const [isOpen, setIsOpen] = useState(false);
  const [question, setQuestion] = useState("");
  const [language, setLanguage] = useState("English");
  const [persona, setPersona] = useState("Professional Assistant");
  const [messages, setMessages] = useState<Message[]>([
    { id: "init-msg", role: "ai", text: "Hello! I am Baymax. Ask me anything about your current operational state." }
  ]);
  const [isLoading, setIsLoading] = useState(false);
  const [isRecording, setIsRecording] = useState(false);
  
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const audioChunksRef = useRef<Blob[]>([]);
  const audioPlayerRef = useRef<HTMLAudioElement | null>(null);

  const handleSubmit = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!question.trim()) return;

    const userQ = question.trim();
    const userMsgId = Date.now().toString();
    setMessages(prev => [...prev, { id: userMsgId, role: "user", text: userQ }]);
    setQuestion("");
    setIsLoading(true);

    try {
      const res = await analyzeWithBaymax({ 
        agentCode: DEMO_AGENT_CODE, 
        question: userQ,
        language,
        persona
      });
      const aiMsgId = (Date.now() + 1).toString();
      setMessages(prev => [...prev, { id: aiMsgId, role: "ai", text: res.answer, structuredData: res }]);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setMessages(prev => [...prev, { id: Date.now().toString(), role: "ai", text: `Error: ${msg}` }]);
    } finally {
      setIsLoading(false);
    }
  };

  const toggleRecording = async () => {
    if (isRecording) {
      mediaRecorderRef.current?.stop();
      setIsRecording(false);
    } else {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        const mediaRecorder = new MediaRecorder(stream);
        mediaRecorderRef.current = mediaRecorder;
        audioChunksRef.current = [];

        mediaRecorder.ondataavailable = (event) => {
          if (event.data.size > 0) {
            audioChunksRef.current.push(event.data);
          }
        };

        mediaRecorder.onstop = async () => {
          const audioBlob = new Blob(audioChunksRef.current, { type: "audio/webm" });
          stream.getTracks().forEach(track => track.stop());
          
          setIsLoading(true);
          try {
            const transcribedText = await transcribeAudio(audioBlob);
            setQuestion(transcribedText);
          } catch (err) {
            console.error("Transcription failed", err);
          } finally {
            setIsLoading(false);
          }
        };

        mediaRecorder.start();
        setIsRecording(true);
      } catch (err) {
        console.error("Microphone access denied", err);
      }
    }
  };

  const playSpeech = async (msgIndex: number, text: string) => {
    try {
      const msg = messages[msgIndex];
      if (msg.audioUrl) {
        if (audioPlayerRef.current) {
          audioPlayerRef.current.pause();
        }
        const audio = new Audio(msg.audioUrl);
        audioPlayerRef.current = audio;
        audio.play();
        return;
      }
      
      setIsLoading(true);
      const url = await generateSpeechUrl(text);
      setMessages(prev => prev.map((m, i) => i === msgIndex ? { ...m, audioUrl: url } : m));
      
      if (audioPlayerRef.current) {
        audioPlayerRef.current.pause();
      }
      const audio = new Audio(url);
      audioPlayerRef.current = audio;
      audio.play();
    } catch (err) {
      console.error("Failed to play speech", err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <button
        onClick={() => setIsOpen(true)}
        className={`fixed bottom-6 right-6 p-4 rounded-full bg-blue-600 text-white shadow-lg hover:bg-blue-700 transition-all z-50 flex items-center justify-center ${isOpen ? 'scale-0' : 'scale-100'}`}
      >
        <Bot size={24} />
      </button>

      <div 
        className={`fixed bottom-6 right-6 w-96 max-w-[calc(100vw-32px)] bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-gray-100 dark:border-slate-700 flex flex-col z-50 overflow-hidden transition-all transform origin-bottom-right duration-300 ${isOpen ? 'scale-100 opacity-100' : 'scale-0 opacity-0 pointer-events-none'}`}
        style={{ height: "600px", maxHeight: "calc(100vh - 100px)" }}
      >
        <div className="bg-blue-600 p-4 flex flex-col gap-3 text-white">
          <div className="flex items-center justify-between font-bold">
            <div className="flex items-center gap-2">
              <Bot size={20} /> Deep Baymax
            </div>
            <button onClick={() => setIsOpen(false)} className="hover:bg-blue-700 p-1 rounded-md transition-colors">
              <X size={20} />
            </button>
          </div>
          
          <div className="flex gap-2 text-xs">
            <select 
              value={language} 
              onChange={(e) => setLanguage(e.target.value)}
              className="bg-blue-700 border border-blue-500 rounded p-1 flex-1 text-white outline-none"
            >
              <option value="English">English</option>
              <option value="Bangla">Bangla</option>
              <option value="Banglish">Banglish</option>
            </select>
            <select 
              value={persona} 
              onChange={(e) => setPersona(e.target.value)}
              className="bg-blue-700 border border-blue-500 rounded p-1 flex-1 text-white outline-none"
            >
              <option value="Professional Assistant">Professional</option>
              <option value="Friendly Advisor">Friendly</option>
              <option value="Direct and Concise">Concise</option>
            </select>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-4 bg-gray-50/50 dark:bg-slate-800/50">
          {messages.map((msg, i) => (
            <div key={msg.id} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
              <div className={`px-4 py-3 rounded-2xl max-w-[85%] text-sm ${msg.role === 'user' ? 'bg-blue-600 text-white rounded-br-sm' : 'bg-white dark:bg-slate-700 border border-gray-100 dark:border-slate-600 text-gray-800 dark:text-gray-100 shadow-sm rounded-bl-sm whitespace-pre-wrap'}`}>
                {msg.text}
                
                {msg.role === 'ai' && (
                  <div className="mt-2 flex items-center justify-end">
                     <button 
                       onClick={() => playSpeech(i, msg.text)}
                       className="text-gray-400 hover:text-blue-600 transition-colors p-1"
                       title="Read aloud"
                     >
                       <Volume2 size={16} />
                     </button>
                  </div>
                )}
                
                {msg.structuredData && <StructuredDataDisplay data={msg.structuredData} />}
              </div>
            </div>
          ))}
          {isLoading && (
            <div className="flex justify-start">
              <div className="px-4 py-2 bg-white dark:bg-slate-700 border border-gray-100 dark:border-slate-600 text-gray-500 dark:text-gray-300 rounded-2xl rounded-bl-sm flex items-center gap-2 shadow-sm">
                <Loader2 size={16} className="animate-spin" /> Baymax is analyzing...
              </div>
            </div>
          )}
        </div>

        <div className="p-3 bg-white dark:bg-slate-900 border-t border-gray-100 dark:border-slate-700">
          <form onSubmit={handleSubmit} className="flex gap-2 items-center">
            <button 
              type="button"
              onClick={toggleRecording}
              className={`p-2 rounded-full transition-colors flex-shrink-0 ${isRecording ? 'bg-red-100 text-red-600 animate-pulse' : 'bg-gray-100 dark:bg-slate-800 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-slate-700'}`}
              title={isRecording ? "Stop recording" : "Record voice"}
            >
              {isRecording ? <Square size={18} className="fill-current" /> : <Mic size={18} />}
            </button>
            <input
              type="text"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              placeholder={isRecording ? "Recording..." : "Ask Deep Baymax..."}
              className="flex-1 px-4 py-2 border border-gray-200 dark:border-slate-600 bg-transparent dark:text-white rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 text-sm"
              disabled={isLoading || isRecording}
            />
            <button 
              type="submit"
              disabled={isLoading || !question.trim() || isRecording}
              className="p-2 rounded-full bg-blue-600 text-white disabled:bg-gray-300 disabled:text-white transition-colors flex-shrink-0"
            >
              <Send size={18} className={question.trim() ? "translate-x-[1px]" : ""} />
            </button>
          </form>
        </div>
      </div>
    </>
  );
}

