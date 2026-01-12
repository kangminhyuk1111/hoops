'use client';

import { Suspense } from 'react';
import KakaoCallbackContent from './KakaoCallbackContent';

export default function KakaoCallbackPage() {
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <KakaoCallbackContent />
    </Suspense>
  );
}

function LoadingSpinner() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-orange-500 border-t-transparent mx-auto mb-4"></div>
        <p className="text-gray-600">로그인 처리 중...</p>
      </div>
    </div>
  );
}
