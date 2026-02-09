import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "경기 생성",
  description: "새로운 농구 경기를 만들고 함께할 사람을 모집하세요",
};

export default function NewMatchLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return children;
}
