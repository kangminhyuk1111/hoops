import type { Metadata } from "next";

const API_URL =
  process.env.INTERNAL_API_URL ||
  process.env.NEXT_PUBLIC_API_URL ||
  "http://localhost:8080";

interface MatchResponse {
  id: number;
  title: string;
  address: string;
  matchDate: string;
  startTime: string;
  currentParticipants: number;
  maxParticipants: number;
}

async function fetchMatch(id: string): Promise<MatchResponse | null> {
  try {
    const response = await fetch(`${API_URL}/api/matches/${id}`, {
      next: { revalidate: 60 },
    });
    if (!response.ok) return null;
    return response.json();
  } catch {
    return null;
  }
}

function formatMatchDescription(match: MatchResponse): string {
  const date = new Date(match.matchDate);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const startTime = match.startTime.slice(0, 5);
  return `${month}월 ${day}일 ${startTime} · ${match.address} · ${match.currentParticipants}/${match.maxParticipants}명 참가 중`;
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ id: string }>;
}): Promise<Metadata> {
  const { id } = await params;
  const match = await fetchMatch(id);

  if (!match) {
    return {
      title: "경기 상세",
      description: "경기 정보를 확인하세요",
    };
  }

  const description = formatMatchDescription(match);

  return {
    title: match.title,
    description,
    openGraph: {
      title: match.title,
      description,
      type: "article",
      images: [
        {
          url: "/og-image.svg",
          width: 1200,
          height: 630,
          alt: match.title,
        },
      ],
    },
    twitter: {
      card: "summary_large_image",
      title: match.title,
      description,
      images: ["/og-image.svg"],
    },
  };
}

export default function MatchDetailLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return children;
}
