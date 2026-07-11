import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import Sidebar from "@/components/Navbar";
import { AppProvider } from "@/context/AppContext";
import { SimulationProvider } from "@/context/SimulationContext";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "BNR Baymax",
  description: "Liquidity and risk decision-support prototype",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <AppProvider>
          <SimulationProvider>
            <Sidebar />
            <main className="ml-[240px] min-h-screen">
              {children}
            </main>
          </SimulationProvider>
        </AppProvider>
      </body>
    </html>
  );
}
