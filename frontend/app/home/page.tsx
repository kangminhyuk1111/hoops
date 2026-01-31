'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/store/auth';
import { Match, MatchListResponse, MatchStatus, MatchSortType } from '@/types';
import api from '@/lib/api';
import MatchCard from '@/components/MatchCard';
import KakaoMap from '@/components/KakaoMap';

// 기본 위치: 서울 시청
const DEFAULT_LOCATION = {
  latitude: 37.5665,
  longitude: 126.978,
};

const DISTANCE_OPTIONS: { label: string; value: number | null }[] = [
  { label: '전체', value: null },
  { label: '1km', value: 1 },
  { label: '3km', value: 3 },
  { label: '5km', value: 5 },
  { label: '10km', value: 10 },
];

const STATUS_FILTER_OPTIONS: { label: string; value: MatchStatus | null }[] = [
  { label: '전체', value: null },
  { label: '모집중', value: 'PENDING' },
  { label: '진행중', value: 'IN_PROGRESS' },
  { label: '종료', value: 'ENDED' },
];

const SORT_OPTIONS: { label: string; value: MatchSortType }[] = [
  { label: '거리순', value: 'DISTANCE' },
  { label: '마감임박', value: 'URGENCY' },
];

export default function Home() {
  const router = useRouter();
  const { isAuthenticated, user, logout } = useAuthStore();
  const [matches, setMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [location, setLocation] = useState(DEFAULT_LOCATION);
  const [locationStatus, setLocationStatus] = useState<'loading' | 'granted' | 'denied' | 'default'>('loading');
  const [distance, setDistance] = useState<number | null>(5);
  const [totalCount, setTotalCount] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [statusFilter, setStatusFilter] = useState<MatchStatus | null>(null);
  const [sortType, setSortType] = useState<MatchSortType>('DISTANCE');

  useEffect(() => {
    console.log('[Home] 컴포넌트 마운트, 위치 요청 시작');
    requestLocation();
  }, []);

  useEffect(() => {
    console.log('[Home] 위치 상태 변경:', {
      locationStatus,
      location,
      distance,
      willShowRedMarker: locationStatus === 'granted'
    });
    if (locationStatus !== 'loading') {
      fetchMatches();
    }
  }, [locationStatus, location, distance, statusFilter, sortType]);

  const requestLocation = () => {
    // Geolocation API 지원 여부 확인
    if (!navigator.geolocation) {
      console.warn('Geolocation API가 지원되지 않는 브라우저입니다.');
      setLocationStatus('default');
      return;
    }

    // HTTPS 확인 (localhost 제외)
    if (typeof window !== 'undefined' &&
        window.location.protocol !== 'https:' &&
        window.location.hostname !== 'localhost' &&
        window.location.hostname !== '127.0.0.1') {
      console.warn('Geolocation API는 HTTPS에서만 동작합니다.');
      setLocationStatus('default');
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        console.log('위치 정보 획득 성공:', position.coords);
        setLocation({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        });
        setLocationStatus('granted');
      },
      (error) => {
        // 에러 코드별 상세 로깅
        const errorMessages: Record<number, string> = {
          1: '사용자가 위치 정보 제공을 거부했습니다.',
          2: '위치 정보를 사용할 수 없습니다.',
          3: '위치 정보 요청 시간이 초과되었습니다.',
        };
        console.warn('위치 정보 획득 실패:', errorMessages[error.code] || error.message);
        setLocationStatus('denied');
      },
      {
        enableHighAccuracy: false,  // true는 GPS 사용, false는 WiFi/IP 기반 (더 빠름)
        timeout: 10000,             // 10초로 증가
        maximumAge: 300000          // 5분간 캐시된 위치 허용
      }
    );
  };

  const fetchMatches = async () => {
    setLoading(true);
    setError(null);

    try {
      const params: Record<string, unknown> = {
        latitude: location.latitude,
        longitude: location.longitude,
        sort: sortType,
      };
      if (distance !== null) {
        params.distance = distance;
      }
      if (statusFilter) {
        params.status = statusFilter;
      }
      const response = await api.get('/api/matches', { params });
      const data = response.data;
      if (Array.isArray(data)) {
        setMatches(data);
        setTotalCount(data.length);
        setHasMore(false);
      } else {
        setMatches(data.items);
        setTotalCount(data.totalCount);
        setHasMore(data.hasMore);
      }
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
      <div className="px-4 py-2 bg-white border-b border-gray-100 z-10 flex-shrink-0 space-y-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-xs text-gray-500">
              {locationStatus === 'granted' && '📍 현재 위치 기준'}
              {locationStatus === 'denied' && '📍 기본 위치 (서울)'}
              {locationStatus === 'default' && '📍 서울'}
              {locationStatus === 'loading' && '📍 위치 확인 중...'}
            </span>
            {(locationStatus === 'denied' || locationStatus === 'default') && (
              <button
                onClick={() => {
                  setLocationStatus('loading');
                  requestLocation();
                }}
                className="text-xs text-orange-500 font-medium"
              >
                위치 재요청
              </button>
            )}
          </div>
          <div className="flex gap-1.5">
            {DISTANCE_OPTIONS.map((opt) => (
              <button
                key={opt.label}
                onClick={() => setDistance(opt.value)}
                className={`px-2.5 py-1 rounded-full text-xs font-medium transition-colors ${
                  distance === opt.value
                    ? 'bg-orange-500 text-white'
                    : 'bg-gray-100 text-gray-600 active:bg-gray-200'
                }`}
              >
                {opt.label}
              </button>
            ))}
          </div>
        </div>
        <div className="flex items-center justify-between">
          <div className="flex gap-1.5">
            {STATUS_FILTER_OPTIONS.map((opt) => (
              <button
                key={opt.label}
                onClick={() => setStatusFilter(opt.value)}
                className={`px-2.5 py-1 rounded-full text-xs font-medium transition-colors ${
                  statusFilter === opt.value
                    ? 'bg-orange-500 text-white'
                    : 'bg-gray-100 text-gray-600 active:bg-gray-200'
                }`}
              >
                {opt.label}
              </button>
            ))}
          </div>
          <div className="flex gap-1.5">
            {SORT_OPTIONS.map((opt) => (
              <button
                key={opt.value}
                onClick={() => setSortType(opt.value)}
                className={`px-2.5 py-1 rounded-full text-xs font-medium transition-colors ${
                  sortType === opt.value
                    ? 'bg-orange-500 text-white'
                    : 'bg-gray-100 text-gray-600 active:bg-gray-200'
                }`}
              >
                {opt.label}
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
            currentLocation={locationStatus !== 'loading' ? location : undefined}
            onMarkerClick={handleMatchClick}
          />
          {/* Match Count Badge */}
          {!loading && matches.length > 0 && (
            <div className="absolute top-3 left-3 bg-white rounded-full px-3 py-1.5 shadow-md">
              <span className="text-sm font-medium text-gray-700">
                경기 <span className="text-orange-500">{totalCount}</span>개
                {distance !== null && ` (${distance}km 이내)`}
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
