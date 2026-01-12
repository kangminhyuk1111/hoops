'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuthStore } from '@/lib/store/auth';
import api from '@/lib/api';

export default function KakaoCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { login, setUser } = useAuthStore();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const code = searchParams.get('code');

    if (!code) {
      setError('인증 코드가 없습니다.');
      return;
    }

    handleKakaoCallback(code);
  }, [searchParams]);

  const handleKakaoCallback = async (code: string) => {
    try {
      // 백엔드로 인증 코드 전송
      const response = await api.get(`/api/auth/kakao/callback?code=${code}`);
      const data = response.data;

      if (data.isNewUser) {
        // 신규 사용자: 회원가입 페이지로 이동 (임시 토큰 저장)
        sessionStorage.setItem('tempToken', data.tempToken);
        router.replace('/signup');
      } else {
        // 기존 사용자: 토큰 저장 후 홈으로 이동
        login(data.accessToken, data.refreshToken);

        // 사용자 정보 조회
        const userResponse = await api.get('/api/users/me', {
          headers: { Authorization: `Bearer ${data.accessToken}` },
        });
        setUser(userResponse.data);

        router.replace('/');
      }
    } catch (err) {
      console.error('카카오 로그인 실패:', err);
      setError('로그인에 실패했습니다. 다시 시도해주세요.');
    }
  };

  if (error) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 px-4">
        <div className="bg-white rounded-2xl shadow-lg p-8 max-w-md w-full text-center">
          <div className="text-red-500 text-5xl mb-4">!</div>
          <h2 className="text-xl font-semibold text-gray-800 mb-2">
            로그인 실패
          </h2>
          <p className="text-gray-600 mb-6">{error}</p>
          <button
            onClick={() => router.replace('/login')}
            className="w-full bg-orange-500 hover:bg-orange-600 text-white font-medium py-3 px-4 rounded-xl transition-colors"
          >
            다시 시도하기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-orange-500 border-t-transparent mx-auto mb-4"></div>
        <p className="text-gray-600">로그인 처리 중...</p>
      </div>
    </div>
  );
}
