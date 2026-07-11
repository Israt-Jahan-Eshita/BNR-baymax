import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import Sidebar from "@/components/Navbar";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "SuperAgent — Liquidity & Risk Intelligence",
  description: "Multi-provider liquidity visibility and risk intelligence platform for mobile financial service agents.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <Sidebar />
        <main className="ml-[240px] min-h-screen">
          {children}
        </main>
      </body>
    </html>
  );
}
