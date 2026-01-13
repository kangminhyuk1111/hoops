'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/store/auth';
import { Match } from '@/types';
import api from '@/lib/api';
import MatchCard from '@/components/MatchCard';
import KakaoMap from '@/components/KakaoMap';

// 기본 위치: 서울 시청
const DEFAULT_LOCATION = {
  latitude: 37.5665,
  longitude: 126.978,
};

const DISTANCE_OPTIONS = [3, 5, 10, 20, 50];

export default function Home() {
  const router = useRouter();
  const { isAuthenticated, user, logout } = useAuthStore();
  const [matches, setMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [location, setLocation] = useState(DEFAULT_LOCATION);
  const [locationStatus, setLocationStatus] = useState<'loading' | 'granted' | 'denied' | 'default'>('loading');
  const [distance, setDistance] = useState(10);

  useEffect(() => {
    requestLocation();
  }, []);

  useEffect(() => {
    if (locationStatus !== 'loading') {
      fetchMatches();
    }
  }, [locationStatus, location, distance]);

  const requestLocation = () => {
    if (!navigator.geolocation) {
      setLocationStatus('default');
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setLocation({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        });
        setLocationStatus('granted');
      },
      () => {
        setLocationStatus('denied');
      },
      { enableHighAccuracy: true, timeout: 5000 }
    );
  };

  const fetchMatches = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await api.get('/api/matches', {
        params: {
          latitude: location.latitude,
          longitude: location.longitude,
          distance: distance,
        },
      });
      setMatches(response.data);
    } catch (err) {
      console.error('경기 목록 조회 실패:', err);
      setError('경기 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  const handleMatchClick = (match: Match) => {
    router.push(`/matches/${match.id}`);
  };

  return (
    <div className="h-screen bg-gray-50 flex flex-col">
      {/* Header */}
      <header className="bg-white shadow-sm z-20 flex-shrink-0">
        <div className="px-4 py-3 flex justify-between items-center">
          <h1 className="text-xl font-bold text-orange-500">HOOPS</h1>
          {isAuthenticated ? (
            <div className="flex items-center gap-3">
              <span className="text-sm text-gray-700 truncate max-w-[100px]">
                {user?.nickname}
              </span>
              <button
                onClick={handleLogout}
                className="text-xs text-gray-500"
              >
                로그아웃
              </button>
            </div>
          ) : (
            <button
              onClick={() => router.push('/login')}
              className="bg-orange-500 text-white px-4 py-2 rounded-lg text-sm"
            >
              로그인
            </button>
          )}
        </div>
      </header>

      {/* Filter Bar */}
      <div className="px-4 py-2 bg-white border-b border-gray-100 z-10 flex-shrink-0">
        <div className="flex items-center justify-between">
          <span className="text-xs text-gray-500">
            {locationStatus === 'granted' && '현재 위치 기준'}
            {locationStatus === 'denied' && '기본 위치 사용'}
            {locationStatus === 'default' && '서울'}
            {locationStatus === 'loading' && '위치 확인 중...'}
          </span>
          <div className="flex gap-1.5">
            {DISTANCE_OPTIONS.map((d) => (
              <button
                key={d}
                onClick={() => setDistance(d)}
                className={`px-2.5 py-1 rounded-full text-xs font-medium transition-colors ${
                  distance === d
                    ? 'bg-orange-500 text-white'
                    : 'bg-gray-100 text-gray-600 active:bg-gray-200'
                }`}
              >
                {d}km
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Content - Map Half + List Half */}
      <main className="flex-1 flex flex-col min-h-0">
        {/* Map Section - 50% */}
        <div className="h-1/2 relative flex-shrink-0">
          <KakaoMap
            matches={matches}
            center={location}
            currentLocation={locationStatus === 'granted' ? location : undefined}
            onMarkerClick={handleMatchClick}
          />
          {/* Match Count Badge */}
          {!loading && matches.length > 0 && (
            <div className="absolute top-3 left-3 bg-white rounded-full px-3 py-1.5 shadow-md">
              <span className="text-sm font-medium text-gray-700">
                경기 <span className="text-orange-500">{matches.length}</span>개
              </span>
            </div>
          )}
        </div>

        {/* List Section - 50% */}
        <div className="h-1/2 flex-shrink-0 overflow-hidden flex flex-col bg-white border-t border-gray-200">
          <div className="px-4 py-2 border-b border-gray-100 flex-shrink-0">
            <span className="text-sm font-medium text-gray-800">주변 경기</span>
          </div>

          <div className="flex-1 overflow-y-auto">
            {loading ? (
              <div className="flex items-center justify-center h-full">
                <div className="animate-spin rounded-full h-6 w-6 border-2 border-orange-500 border-t-transparent"></div>
              </div>
            ) : error ? (
              <div className="flex items-center justify-center h-full">
                <div className="text-center">
                  <p className="text-gray-500 text-sm mb-2">{error}</p>
                  <button
                    onClick={fetchMatches}
                    className="text-orange-500 text-sm font-medium"
                  >
                    다시 시도
                  </button>
                </div>
              </div>
            ) : matches.length === 0 ? (
              <div className="flex items-center justify-center h-full">
                <p className="text-gray-400 text-sm">주변에 등록된 경기가 없습니다.</p>
              </div>
            ) : (
              <div className="p-3 space-y-2">
                {matches.map((match) => (
                  <MatchCard
                    key={match.id}
                    match={match}
                    onClick={() => handleMatchClick(match)}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      </main>

      {/* Bottom Navigation */}
      {isAuthenticated && (
        <nav className="bg-white border-t border-gray-200 z-10 flex-shrink-0">
          <div className="flex justify-around py-2">
            <button className="flex flex-col items-center py-1 px-3 text-orange-500">
              <HomeIcon />
              <span className="text-xs mt-1">홈</span>
            </button>
            <button
              onClick={() => router.push('/matches/new')}
              className="flex flex-col items-center py-1 px-3 text-gray-400"
            >
              <PlusIcon />
              <span className="text-xs mt-1">경기생성</span>
            </button>
            <button
              onClick={() => router.push('/mypage')}
              className="flex flex-col items-center py-1 px-3 text-gray-400"
            >
              <UserIcon />
              <span className="text-xs mt-1">마이</span>
            </button>
          </div>
        </nav>
      )}
    </div>
  );
}

function HomeIcon() {
  return (
    <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
      <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" />
    </svg>
  );
}

function PlusIcon() {
  return (
    <svg className="w-6 h-6" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
    </svg>
  );
}

function UserIcon() {
  return (
    <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
    </svg>
  );
}
