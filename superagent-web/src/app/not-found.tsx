"use client";

import Link from "next/link";
import { AlertCircle } from "lucide-react";

export default function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[70vh]">
      <div className="p-8 neu-inset rounded-3xl flex flex-col items-center text-center max-w-md">
        <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center text-red-500 mb-6">
          <AlertCircle size={32} />
        </div>
        <h1 className="text-3xl font-bold text-gray-800 mb-2">404 - Not Found</h1>
        <p className="text-gray-500 mb-8">
          The operational view you are looking for does not exist or has been moved.
        </p>
        <Link 
          href="/"
          className="px-6 py-3 neu-raised text-blue-600 font-bold rounded-xl hover:text-blue-700 transition-all"
        >
          Return to Dashboard
        </Link>
      </div>
    </div>
  );
}
