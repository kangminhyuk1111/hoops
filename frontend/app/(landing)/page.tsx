'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/lib/store/auth';

export default function LandingPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuthStore();

  useEffect(() => {
    if (isAuthenticated) {
      router.replace('/home');
    }
  }, [isAuthenticated, router]);

  if (isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-white">
      {/* Navigation */}
      <nav className="fixed top-0 w-full bg-white/95 backdrop-blur-sm z-50 border-b border-gray-100">
        <div className="max-w-[1200px] mx-auto px-6 py-4 flex justify-between items-center">
          <span className="text-2xl font-extrabold text-orange-500 tracking-tight">HOOPS</span>
          <Link
            href="/login"
            className="bg-orange-500 hover:bg-orange-600 text-white px-6 py-2.5 rounded-lg text-sm font-semibold transition-colors"
          >
            시작하기
          </Link>
        </div>
      </nav>

      {/* Hero */}
      <section className="pt-[140px] pb-20 px-6 text-center bg-gradient-to-b from-orange-50 to-white">
        <span className="inline-block bg-orange-50 border border-orange-300 text-orange-600 px-4 py-1.5 rounded-full text-[13px] font-semibold mb-6">
          위치 기반 농구 매칭 플랫폼
        </span>
        <h1 className="text-[clamp(32px,5vw,56px)] font-extrabold leading-[1.2] text-gray-900 mb-5 tracking-tight">
          농구하고 싶을 때,<br />
          <span className="text-orange-500">가까운 경기</span>를 찾아보세요
        </h1>
        <p className="text-[clamp(16px,2vw,20px)] text-gray-500 max-w-[560px] mx-auto mb-10">
          내 주변에서 열리는 농구 경기를 검색하고, 참여하거나 직접 경기를 만들어 함께 할 사람을 모아보세요.
        </p>
        <Link
          href="/login"
          className="inline-flex items-center gap-2 bg-orange-500 hover:bg-orange-600 text-white px-9 py-4 rounded-xl text-lg font-bold transition-all hover:-translate-y-0.5"
        >
          지금 시작하기
        </Link>
        <p className="mt-4 text-[13px] text-gray-400">카카오 로그인으로 간편하게 시작</p>
      </section>

      {/* Phone Mockup */}
      <section className="text-center px-6 pb-20">
        <div className="max-w-[320px] mx-auto bg-gray-900 rounded-[36px] p-3 shadow-2xl">
          <div className="bg-white rounded-[28px] overflow-hidden">
            {/* Phone Header */}
            <div className="px-4 py-4 flex justify-between items-center border-b border-gray-100">
              <span className="text-lg font-extrabold text-orange-500">HOOPS</span>
              <span className="text-xs text-gray-400">로그인</span>
            </div>
            {/* Phone Map */}
            <div className="h-[180px] bg-green-50 relative overflow-hidden">
              <div
                className="absolute inset-0 opacity-50"
                style={{
                  backgroundImage:
                    'linear-gradient(90deg, transparent 48%, #C8DCC8 48%, #C8DCC8 52%, transparent 52%), linear-gradient(0deg, transparent 48%, #C8DCC8 48%, #C8DCC8 52%, transparent 52%)',
                  backgroundSize: '40px 40px',
                }}
              />
              <div className="absolute w-6 h-6 rounded-full border-[3px] border-white shadow-md bg-green-500 top-10 left-[60px]" />
              <div className="absolute w-6 h-6 rounded-full border-[3px] border-white shadow-md bg-orange-500 top-20 left-[160px]" />
              <div className="absolute w-6 h-6 rounded-full border-[3px] border-white shadow-md bg-red-500 top-[50px] right-[70px]" />
              <div className="absolute w-6 h-6 rounded-full border-[3px] border-white shadow-md bg-green-500 bottom-10 left-[120px]" />
            </div>
            {/* Phone List */}
            <div className="p-3 space-y-2">
              <MockupCard
                title="강남 3:3 농구"
                badge="모집중"
                badgeClass="bg-green-100 text-green-700"
                location="강남구 역삼동 농구코트"
                distance="1.2km"
                participants="3/6명"
              />
              <MockupCard
                title="잠실 5:5 풀코트"
                badge="마감임박"
                badgeClass="bg-amber-100 text-amber-700"
                location="송파구 잠실종합운동장"
                distance="2.8km"
                participants="9/10명"
              />
            </div>
          </div>
        </div>
      </section>

      {/* Stats */}
      <div className="flex justify-center gap-16 sm:gap-[60px] px-6 py-10 flex-wrap">
        <Stat value="3단계" label="참여까지 걸리는 과정" />
        <Stat value="10km" label="검색 최대 반경" />
        <Stat value="실시간" label="위치 기반 경기 검색" />
      </div>

      {/* Features */}
      <section className="max-w-[1000px] mx-auto px-6 py-20">
        <h2 className="text-center text-[clamp(24px,3.5vw,36px)] font-extrabold text-gray-900 mb-3 tracking-tight">
          왜 HOOPS인가요?
        </h2>
        <p className="text-center text-base text-gray-500 mb-[60px]">
          농구 경기를 찾고 참여하는 가장 쉬운 방법
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
          <FeatureCard icon="📍" bgClass="bg-orange-50" title="위치 기반 검색" desc="내 위치 주변 1km~10km 반경의 농구 경기를 실시간으로 검색할 수 있습니다. 지도에서 한눈에 경기를 확인하세요." />
          <FeatureCard icon="🏀" bgClass="bg-green-50" title="간편한 경기 생성" desc="장소, 시간, 인원만 설정하면 바로 경기를 만들 수 있습니다. 함께 할 사람을 쉽게 모아보세요." />
          <FeatureCard icon="👥" bgClass="bg-blue-50" title="참여 관리" desc="호스트가 참여 요청을 승인/거절할 수 있어 원하는 멤버와 경기할 수 있습니다." />
          <FeatureCard icon="🗺️" bgClass="bg-purple-50" title="지도 + 리스트 뷰" desc="지도에서 경기 위치를 확인하고, 리스트에서 상세 정보를 확인하세요. 마감 임박 경기는 색상으로 구분됩니다." />
          <FeatureCard icon="⏰" bgClass="bg-red-50" title="마감 임박 알림" desc="경기 시작 24시간 전은 빨간색, 72시간 전은 주황색으로 표시되어 놓치지 않도록 도와줍니다." />
          <FeatureCard icon="⚡" bgClass="bg-yellow-50" title="카카오 간편 로그인" desc="카카오 계정으로 3초 만에 로그인. 복잡한 회원가입 없이 바로 시작할 수 있습니다." />
        </div>
      </section>

      {/* How It Works */}
      <section className="bg-gray-50 py-20 px-6">
        <h2 className="text-center text-[clamp(24px,3.5vw,36px)] font-extrabold text-gray-900 mb-3 tracking-tight">
          어떻게 사용하나요?
        </h2>
        <p className="text-center text-base text-gray-500 mb-[60px]">
          3단계로 농구 경기에 참여하세요
        </p>
        <div className="max-w-[800px] mx-auto flex flex-col">
          <Step num={1} title="주변 경기 검색" desc="앱을 열면 현재 위치 기반으로 주변 농구 경기가 지도와 리스트에 표시됩니다. 거리, 상태, 마감 임박순으로 필터링하세요." isLast={false} />
          <Step num={2} title="경기 참여 요청" desc="마음에 드는 경기를 선택하고 참여 요청을 보내세요. 장소, 시간, 현재 인원, 호스트 정보를 확인한 후 결정할 수 있습니다." isLast={false} />
          <Step num={3} title="승인 후 참여" desc="호스트가 참여를 승인하면 확정됩니다. 다른 참가자 정보를 확인하고 경기 당일에 만나세요!" isLast={true} />
        </div>
      </section>

      {/* Personas */}
      <section className="max-w-[900px] mx-auto px-6 py-20">
        <h2 className="text-center text-[clamp(24px,3.5vw,36px)] font-extrabold text-gray-900 mb-3 tracking-tight">
          이런 분들을 위해 만들었어요
        </h2>
        <p className="text-center text-base text-gray-500 mb-[60px]">
          농구를 좋아하지만 함께 할 사람을 찾기 어려운 모든 분
        </p>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <PersonaCard
            role="Player"
            title="퇴근 후 농구 한 판 하고 싶은 직장인"
            quote="대학 때는 매일 농구했는데, 직장 다니니까 같이 할 사람을 찾기가 너무 힘들어요. 카카오톡 단톡방은 연락이 늦고..."
            tags={['20~30대', '주말/퇴근 후', '동네 농구']}
          />
          <PersonaCard
            role="Host"
            title="농구 동호회를 운영하는 리더"
            quote="매번 카톡으로 인원 모으고, 출석 체크하고... 참여 관리가 정말 번거로워요. 체계적으로 관리하고 싶어요."
            tags={['20~40대', '정기 모임', '인원 관리']}
          />
        </div>
      </section>

      {/* CTA Bottom */}
      <section className="bg-gray-900 py-20 px-6 text-center">
        <h2 className="text-[clamp(24px,4vw,40px)] font-extrabold text-white mb-4 tracking-tight">
          오늘, 내 주변 농구 경기를<br />찾아보세요
        </h2>
        <p className="text-base text-gray-400 mb-9">
          카카오 로그인 한 번이면 바로 시작할 수 있어요.
        </p>
        <Link
          href="/login"
          className="inline-flex items-center gap-2 bg-orange-500 hover:bg-orange-600 text-white px-8 py-3.5 rounded-xl text-base font-bold transition-colors"
        >
          HOOPS 시작하기
        </Link>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 border-t border-white/10 py-8 px-6 text-center">
        <p className="text-orange-500 font-extrabold text-base mb-2">HOOPS</p>
        <p className="text-[13px] text-gray-500">&copy; 2026 HOOPS. All rights reserved.</p>
      </footer>
    </div>
  );
}

/* ─── Sub Components ─── */

function MockupCard({
  title,
  badge,
  badgeClass,
  location,
  distance,
  participants,
}: {
  title: string;
  badge: string;
  badgeClass: string;
  location: string;
  distance: string;
  participants: string;
}) {
  return (
    <div className="bg-white rounded-xl p-3.5 border border-gray-100">
      <div className="flex justify-between items-center mb-1.5">
        <span className="text-[13px] font-bold text-gray-800">{title}</span>
        <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${badgeClass}`}>{badge}</span>
      </div>
      <p className="text-[11px] text-gray-500">{location}</p>
      <div className="flex justify-between items-center mt-2 text-[11px]">
        <span className="text-gray-400">{distance}</span>
        <span className="text-orange-500 font-semibold">{participants}</span>
      </div>
    </div>
  );
}

function Stat({ value, label }: { value: string; label: string }) {
  return (
    <div className="text-center">
      <div className="text-4xl sm:text-[36px] font-extrabold text-orange-500">{value}</div>
      <div className="text-sm text-gray-500 mt-1">{label}</div>
    </div>
  );
}

function FeatureCard({ icon, bgClass, title, desc }: { icon: string; bgClass: string; title: string; desc: string }) {
  return (
    <div className="bg-white border border-gray-100 rounded-2xl p-8 transition-shadow hover:shadow-lg">
      <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-2xl mb-5 ${bgClass}`}>
        {icon}
      </div>
      <h3 className="text-lg font-bold text-gray-800 mb-2">{title}</h3>
      <p className="text-sm text-gray-500 leading-relaxed">{desc}</p>
    </div>
  );
}

function Step({ num, title, desc, isLast }: { num: number; title: string; desc: string; isLast: boolean }) {
  return (
    <div className="flex gap-4 sm:gap-6 items-start relative pb-10 last:pb-0">
      <div className="relative z-10">
        <div className="w-11 h-11 min-w-[44px] rounded-full bg-orange-500 text-white flex items-center justify-center text-lg font-extrabold">
          {num}
        </div>
        {!isLast && (
          <div className="absolute top-11 left-1/2 -translate-x-1/2 w-0.5 h-[calc(100%+0px)] bg-orange-300" />
        )}
      </div>
      <div className="pt-2">
        <h3 className="text-lg font-bold text-gray-800 mb-1.5">{title}</h3>
        <p className="text-sm text-gray-500 leading-relaxed">{desc}</p>
      </div>
    </div>
  );
}

function PersonaCard({ role, title, quote, tags }: { role: string; title: string; quote: string; tags: string[] }) {
  return (
    <div className="border border-gray-100 rounded-2xl p-8 bg-white">
      <div className="text-xs font-semibold text-orange-500 uppercase mb-1">{role}</div>
      <h3 className="text-xl font-bold text-gray-800 mb-3">{title}</h3>
      <p className="text-sm text-gray-500 italic leading-relaxed border-l-[3px] border-orange-500 pl-4 mb-4">
        &ldquo;{quote}&rdquo;
      </p>
      <div className="flex gap-2 flex-wrap">
        {tags.map((tag) => (
          <span key={tag} className="text-xs px-3 py-1 rounded-full bg-gray-50 text-gray-600">{tag}</span>
        ))}
      </div>
    </div>
  );
}
