export default function AppLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div className="mx-auto w-full max-w-[430px] min-h-screen bg-gray-50 shadow-xl">
      {children}
    </div>
  );
}
