import type { Metadata, Viewport } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { Toaster } from "react-hot-toast";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  metadataBase: new URL(process.env.NEXT_PUBLIC_SITE_URL || "https://hoops.kr"),
  title: {
    default: "HOOPS - 농구 경기 매칭",
    template: "%s | HOOPS",
  },
  description: "근처 농구 경기를 찾아 참가하세요",
  openGraph: {
    type: "website",
    locale: "ko_KR",
    siteName: "HOOPS",
    title: "HOOPS - 농구 경기 매칭",
    description: "근처 농구 경기를 찾아 참가하세요",
    images: [
      {
        url: "/og-image.svg",
        width: 1200,
        height: 630,
        alt: "HOOPS - 농구 경기 매칭",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "HOOPS - 농구 경기 매칭",
    description: "근처 농구 경기를 찾아 참가하세요",
    images: ["/og-image.svg"],
  },
  appleWebApp: {
    capable: true,
    statusBarStyle: "default",
    title: "HOOPS",
  },
  formatDetection: {
    telephone: false,
  },
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  themeColor: "#f97316",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <head>
        <link rel="stylesheet" href="https://static.toss.im/tps/main.css" />
        <link rel="stylesheet" href="https://static.toss.im/tps/others.css" />
      </head>
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased bg-gray-200`}
        style={{ fontFamily: "'Toss Product Sans', -apple-system, BlinkMacSystemFont, sans-serif" }}
      >
        <div className="mx-auto w-full max-w-[430px] min-h-screen bg-gray-50 shadow-xl">
          {children}
        </div>
        <Toaster
          position="bottom-center"
          toastOptions={{
            duration: 3000,
            style: {
              background: '#333',
              color: '#fff',
              borderRadius: '8px',
              fontSize: '14px',
            },
            success: {
              iconTheme: {
                primary: '#22c55e',
                secondary: '#fff',
              },
            },
            error: {
              iconTheme: {
                primary: '#ef4444',
                secondary: '#fff',
              },
            },
          }}
        />
      </body>
    </html>
  );
}
