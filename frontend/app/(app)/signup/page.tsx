'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/store/auth';
import api from '@/lib/api';

export default function SignupPage() {
  const router = useRouter();
  const { login, setUser, isAuthenticated } = useAuthStore();
  const [nickname, setNickname] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      router.replace('/home');
      return;
    }

    const tempToken = sessionStorage.getItem('tempToken');
    if (!tempToken) {
      router.replace('/login');
    }
  }, [isAuthenticated, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!nickname.trim()) {
      setError('닉네임을 입력해주세요.');
      return;
    }

    if (nickname.length < 2 || nickname.length > 20) {
      setError('닉네임은 2~20자 사이로 입력해주세요.');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const tempToken = sessionStorage.getItem('tempToken');

      const response = await api.post('/api/auth/signup', {
        tempToken,
        nickname: nickname.trim(),
      });

      const { accessToken, refreshToken } = response.data;

      login(accessToken, refreshToken);
      sessionStorage.removeItem('tempToken');

      const userResponse = await api.get('/api/users/me', {
        headers: { Authorization: `Bearer ${accessToken}` },
      });
      setUser(userResponse.data);

      router.replace('/home');
    } catch (err: unknown) {
      const error = err as { response?: { status: number; data?: { message: string } } };
      if (error.response?.status === 409) {
        setError('이미 사용 중인 닉네임입니다.');
      } else if (error.response?.status === 400) {
        setError(error.response.data?.message || '유효하지 않은 요청입니다.');
      } else {
        setError('회원가입에 실패했습니다. 다시 시도해주세요.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      {/* Header */}
      <div className="flex-1 flex flex-col items-center justify-center px-6 pt-12 pb-6">
        <h1 className="text-4xl font-bold text-orange-500 mb-2">HOOPS</h1>
        <p className="text-gray-600">프로필을 설정해주세요</p>
      </div>

      {/* Signup Form */}
      <div className="px-4 pb-8">
        <div className="bg-white rounded-2xl shadow p-6">
          <h2 className="text-lg font-semibold text-gray-800 text-center mb-6">
            회원가입
          </h2>

          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label
                htmlFor="nickname"
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                닉네임
              </label>
              <input
                type="text"
                id="nickname"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                placeholder="사용할 닉네임을 입력하세요"
                className="w-full px-4 py-3.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all text-base"
                maxLength={20}
                disabled={isLoading}
                autoComplete="off"
              />
              <p className="text-xs text-gray-500 mt-2">
                2~20자 사이로 입력해주세요
              </p>
            </div>

            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}

            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-orange-500 active:bg-orange-600 disabled:bg-orange-300 text-white font-medium py-3.5 px-4 rounded-xl transition-colors"
            >
              {isLoading ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent"></span>
                  처리 중...
                </span>
              ) : (
                '가입 완료'
              )}
            </button>
          </form>
        </div>

        {/* Bottom Info */}
        <p className="text-center text-sm text-gray-500 mt-6">
          닉네임은 나중에 변경할 수 있습니다.
        </p>
      </div>
    </div>
  );
}
