import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "마이페이지",
  description: "내 프로필과 참가 경기를 확인하세요",
  robots: { index: false },
};

export default function MyPageLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return children;
}
