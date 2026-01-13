'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/store/auth';

const KAKAO_CLIENT_ID = process.env.NEXT_PUBLIC_KAKAO_CLIENT_ID;
const KAKAO_REDIRECT_URI = process.env.NEXT_PUBLIC_KAKAO_REDIRECT_URI;

export default function LoginPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuthStore();

  useEffect(() => {
    if (isAuthenticated) {
      router.replace('/');
    }
  }, [isAuthenticated, router]);

  const handleKakaoLogin = () => {
    const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${KAKAO_REDIRECT_URI}&response_type=code`;
    window.location.href = kakaoAuthUrl;
  };

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      {/* Logo Section */}
      <div className="flex-1 flex flex-col items-center justify-center px-6 pt-16 pb-8">
        <h1 className="text-5xl font-bold text-orange-500 mb-3">HOOPS</h1>
        <p className="text-gray-600 text-center">
          근처 농구 경기를 찾아 참가하세요
        </p>
      </div>

      {/* Login Card */}
      <div className="px-4 pb-8">
        <div className="bg-white rounded-2xl shadow p-6">
          <h2 className="text-lg font-semibold text-gray-800 text-center mb-6">
            로그인
          </h2>

          {/* Kakao Login Button */}
          <button
            onClick={handleKakaoLogin}
            className="w-full flex items-center justify-center gap-3 bg-[#FEE500] active:bg-[#FDD800] text-[#191919] font-medium py-3.5 px-4 rounded-xl transition-colors"
          >
            <KakaoIcon />
            카카오로 시작하기
          </button>

          <p className="text-xs text-gray-500 text-center mt-6 leading-relaxed">
            로그인 시{' '}
            <span className="text-orange-500">이용약관</span> 및{' '}
            <span className="text-orange-500">개인정보처리방침</span>에
            동의하게 됩니다.
          </p>
        </div>

        {/* Bottom Info */}
        <p className="text-center text-sm text-gray-500 mt-6">
          계정이 없으신가요? 카카오로 간편하게 가입하세요.
        </p>
      </div>
    </div>
  );
}

function KakaoIcon() {
  return (
    <svg
      width="20"
      height="20"
      viewBox="0 0 24 24"
      fill="currentColor"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        d="M12 3C6.48 3 2 6.58 2 11C2 13.84 3.92 16.34 6.84 17.78L5.77 21.53C5.67 21.87 6.06 22.14 6.36 21.93L10.86 18.87C11.23 18.91 11.61 18.93 12 18.93C17.52 18.93 22 15.35 22 10.93C22 6.58 17.52 3 12 3Z"
        fill="#191919"
      />
    </svg>
  );
}
