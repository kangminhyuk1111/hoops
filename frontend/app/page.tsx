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
    <div className="min-h-screen bg-white flex flex-col">
      {/* Header */}
      <header className="px-5 pt-12 pb-2">
        <span className="text-2xl font-extrabold text-orange-500 tracking-tight">HOOPS</span>
      </header>

      {/* Hero */}
      <section className="px-5 pt-6 pb-8">
        <span className="inline-block bg-orange-50 border border-orange-200 text-orange-600 px-3 py-1 rounded-full text-[11px] font-semibold mb-4">
          위치 기반 농구 매칭
        </span>
        <h1 className="text-[28px] font-extrabold leading-[1.25] text-gray-900 mb-3 tracking-tight">
          농구하고 싶을 때,<br />
          <span className="text-orange-500">가까운 경기</span>를<br />
          찾아보세요
        </h1>
        <p className="text-[15px] text-gray-500 leading-relaxed">
          내 주변 농구 경기를 검색하고 참여하거나,
          직접 경기를 만들어보세요.
        </p>
      </section>

      {/* App Preview Card */}
      <section className="px-5 pb-6">
        <div className="bg-gray-50 rounded-2xl overflow-hidden border border-gray-100">
          {/* Mini Map */}
          <div className="h-[140px] bg-green-50 relative overflow-hidden">
            <div
              className="absolute inset-0 opacity-40"
              style={{
                backgroundImage:
                  'linear-gradient(90deg, transparent 48%, #C8DCC8 48%, #C8DCC8 52%, transparent 52%), linear-gradient(0deg, transparent 48%, #C8DCC8 48%, #C8DCC8 52%, transparent 52%)',
                backgroundSize: '32px 32px',
              }}
            />
            <div className="absolute w-5 h-5 rounded-full border-2 border-white shadow bg-green-500 top-8 left-10" />
            <div className="absolute w-5 h-5 rounded-full border-2 border-white shadow bg-orange-500 top-16 left-[55%]" />
            <div className="absolute w-5 h-5 rounded-full border-2 border-white shadow bg-red-500 top-10 right-12" />
            <div className="absolute w-5 h-5 rounded-full border-2 border-white shadow bg-green-500 bottom-8 left-[40%]" />
            {/* Badge */}
            <div className="absolute top-3 left-3 bg-white/90 backdrop-blur-sm rounded-full px-2.5 py-1 shadow-sm">
              <span className="text-[11px] font-medium text-gray-600">
                경기 <span className="text-orange-500 font-bold">4</span>개
              </span>
            </div>
          </div>
          {/* Mini Cards */}
          <div className="p-3 space-y-2">
            <PreviewCard
              title="강남 3:3 농구"
              badge="모집중"
              badgeClass="bg-green-100 text-green-700"
              location="강남구 역삼동 농구코트"
              distance="1.2km"
              participants="3/6명"
            />
            <PreviewCard
              title="잠실 5:5 풀코트"
              badge="마감임박"
              badgeClass="bg-amber-100 text-amber-700"
              location="송파구 잠실종합운동장"
              distance="2.8km"
              participants="9/10명"
            />
          </div>
        </div>
      </section>

      {/* CTA Button */}
      <section className="px-5 pb-6">
        <Link
          href="/home"
          className="block w-full bg-orange-500 active:bg-orange-600 text-white text-center py-4 rounded-xl text-base font-bold transition-colors"
        >
          시작하기
        </Link>
        <p className="text-center mt-3 text-[12px] text-gray-400">
          카카오 로그인으로 간편하게 시작
        </p>
      </section>

      {/* Stats */}
      <section className="flex justify-around px-5 py-6 border-t border-gray-100">
        <MiniStat value="3단계" label="참여 과정" />
        <MiniStat value="10km" label="최대 반경" />
        <MiniStat value="실시간" label="경기 검색" />
      </section>

      {/* Features */}
      <section className="px-5 py-6">
        <h2 className="text-lg font-extrabold text-gray-900 mb-4">주요 기능</h2>
        <div className="space-y-3">
          <FeatureRow icon="📍" title="위치 기반 검색" desc="내 주변 1~10km 반경 농구 경기 실시간 검색" />
          <FeatureRow icon="🏀" title="간편한 경기 생성" desc="장소, 시간, 인원만 설정하면 바로 생성" />
          <FeatureRow icon="👥" title="참여 관리" desc="호스트가 참여 요청을 승인/거절" />
          <FeatureRow icon="🗺️" title="지도 + 리스트" desc="지도와 리스트에서 동시에 경기 확인" />
          <FeatureRow icon="⏰" title="마감 임박 표시" desc="긴급도에 따라 색상으로 구분" />
          <FeatureRow icon="⚡" title="카카오 로그인" desc="3초 만에 간편 로그인" />
        </div>
      </section>

      {/* How It Works */}
      <section className="px-5 py-6 bg-gray-50">
        <h2 className="text-lg font-extrabold text-gray-900 mb-5">이용 방법</h2>
        <div className="space-y-5">
          <StepRow num={1} title="주변 경기 검색" desc="현재 위치 기반으로 주변 농구 경기가 표시됩니다" />
          <StepRow num={2} title="경기 참여 요청" desc="마음에 드는 경기를 선택하고 참여 요청을 보내세요" />
          <StepRow num={3} title="승인 후 참여" desc="호스트 승인 후 확정! 경기 당일에 만나세요" />
        </div>
      </section>

      {/* Persona */}
      <section className="px-5 py-6">
        <h2 className="text-lg font-extrabold text-gray-900 mb-4">이런 분께 추천해요</h2>
        <div className="space-y-3">
          <PersonaRow
            emoji="🏃"
            title="퇴근 후 농구하고 싶은 직장인"
            desc="같이 할 사람을 찾기 어려운 20~30대"
          />
          <PersonaRow
            emoji="📋"
            title="동호회를 운영하는 리더"
            desc="카톡 대신 체계적으로 인원을 관리하고 싶은 분"
          />
        </div>
      </section>

      {/* Bottom CTA */}
      <section className="px-5 pt-6 pb-10 bg-gray-900">
        <h2 className="text-xl font-extrabold text-white mb-2 leading-tight">
          오늘, 내 주변<br />농구 경기를 찾아보세요
        </h2>
        <p className="text-sm text-gray-400 mb-5">
          카카오 로그인 한 번이면 시작할 수 있어요.
        </p>
        <Link
          href="/home"
          className="block w-full bg-orange-500 active:bg-orange-600 text-white text-center py-3.5 rounded-xl text-[15px] font-bold transition-colors"
        >
          HOOPS 시작하기
        </Link>
        <p className="text-center mt-6 text-orange-500 font-extrabold text-sm">HOOPS</p>
        <p className="text-center mt-1 text-[11px] text-gray-600">&copy; 2026 HOOPS. All rights reserved.</p>
      </section>
    </div>
  );
}

/* ─── Sub Components ─── */

function PreviewCard({
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
    <div className="bg-white rounded-xl p-3 border border-gray-100">
      <div className="flex justify-between items-center mb-1">
        <span className="text-[13px] font-bold text-gray-800">{title}</span>
        <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${badgeClass}`}>{badge}</span>
      </div>
      <p className="text-[11px] text-gray-400">{location}</p>
      <div className="flex justify-between items-center mt-1.5 text-[11px]">
        <span className="text-gray-400">{distance}</span>
        <span className="text-orange-500 font-semibold">{participants}</span>
      </div>
    </div>
  );
}

function MiniStat({ value, label }: { value: string; label: string }) {
  return (
    <div className="text-center">
      <div className="text-lg font-extrabold text-orange-500">{value}</div>
      <div className="text-[11px] text-gray-400 mt-0.5">{label}</div>
    </div>
  );
}

function FeatureRow({ icon, title, desc }: { icon: string; title: string; desc: string }) {
  return (
    <div className="flex items-start gap-3">
      <div className="w-9 h-9 rounded-lg bg-orange-50 flex items-center justify-center text-lg shrink-0">
        {icon}
      </div>
      <div className="min-w-0">
        <h3 className="text-[14px] font-bold text-gray-800">{title}</h3>
        <p className="text-[12px] text-gray-400 leading-relaxed">{desc}</p>
      </div>
    </div>
  );
}

function StepRow({ num, title, desc }: { num: number; title: string; desc: string }) {
  return (
    <div className="flex items-start gap-3">
      <div className="w-7 h-7 rounded-full bg-orange-500 text-white flex items-center justify-center text-[13px] font-bold shrink-0 mt-0.5">
        {num}
      </div>
      <div className="min-w-0">
        <h3 className="text-[14px] font-bold text-gray-800">{title}</h3>
        <p className="text-[12px] text-gray-400 leading-relaxed">{desc}</p>
      </div>
    </div>
  );
}

function PersonaRow({ emoji, title, desc }: { emoji: string; title: string; desc: string }) {
  return (
    <div className="flex items-start gap-3 bg-gray-50 rounded-xl p-4">
      <span className="text-2xl shrink-0">{emoji}</span>
      <div className="min-w-0">
        <h3 className="text-[14px] font-bold text-gray-800">{title}</h3>
        <p className="text-[12px] text-gray-400 leading-relaxed">{desc}</p>
      </div>
    </div>
  );
}
