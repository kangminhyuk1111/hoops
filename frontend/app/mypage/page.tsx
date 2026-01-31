'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/store/auth';
import api from '@/lib/api';
import { Match, ParticipationStatus } from '@/types';

interface UserProfile {
  id: number;
  email: string;
  nickname: string;
  profileImage?: string;
  rating: number;
  totalMatches: number;
}

interface MyParticipation {
  id: number;
  matchId: number;
  userId: number;
  status: ParticipationStatus;
  joinedAt: string;
}

type TabType = 'participated' | 'hosted';

export default function MyPage() {
  const router = useRouter();
  const { isAuthenticated, user, logout } = useAuthStore();

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [participations, setParticipations] = useState<MyParticipation[]>([]);
  const [hostedMatches, setHostedMatches] = useState<Match[]>([]);
  const [participatedMatches, setParticipatedMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<TabType>('participated');

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }
    fetchData();
  }, [isAuthenticated]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [profileRes, participationsRes, hostedRes] = await Promise.all([
        api.get('/api/users/me'),
        api.get('/api/users/me/participations'),
        api.get('/api/matches/hosted'),
      ]);

      setProfile(profileRes.data);
      setParticipations(participationsRes.data);
      setHostedMatches(hostedRes.data);

      // Fetch match details for participations
      const matchIds = participationsRes.data.map((p: MyParticipation) => p.matchId);
      const uniqueMatchIds = [...new Set(matchIds)] as number[];

      const matchPromises = uniqueMatchIds.map((id) =>
        api.get(`/api/matches/${id}`).catch(() => null)
      );
      const matchResults = await Promise.all(matchPromises);
      const matches = matchResults
        .filter((res) => res !== null)
        .map((res) => res!.data);
      setParticipatedMatches(matches);
    } catch (err) {
      console.error('Failed to fetch data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  const getParticipationStatus = (matchId: number): ParticipationStatus | null => {
    const participation = participations.find((p) => p.matchId === matchId);
    return participation?.status || null;
  };

  const getStatusBadge = (status: ParticipationStatus | string | null) => {
    switch (status) {
      case 'PENDING':
        return <span className="px-2 py-0.5 bg-yellow-100 text-yellow-700 text-xs rounded-full">대기중</span>;
      case 'CONFIRMED':
        return <span className="px-2 py-0.5 bg-green-100 text-green-700 text-xs rounded-full">확정</span>;
      case 'REJECTED':
        return <span className="px-2 py-0.5 bg-red-100 text-red-700 text-xs rounded-full">거절됨</span>;
      case 'CANCELLED':
        return <span className="px-2 py-0.5 bg-gray-100 text-gray-700 text-xs rounded-full">취소됨</span>;
      case 'IN_PROGRESS':
        return <span className="px-2 py-0.5 bg-orange-100 text-orange-700 text-xs rounded-full">진행중</span>;
      case 'ENDED':
        return <span className="px-2 py-0.5 bg-gray-100 text-gray-700 text-xs rounded-full">종료</span>;
      default:
        return null;
    }
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric',
      weekday: 'short',
    });
  };

  const formatTime = (timeStr: string) => {
    return timeStr.slice(0, 5);
  };

  if (!isAuthenticated) {
    return null;
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-2 border-orange-500 border-t-transparent"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-20">
        <div className="px-4 py-3 flex items-center justify-between">
          <h1 className="text-lg font-semibold text-gray-800">마이페이지</h1>
          <button
            onClick={handleLogout}
            className="text-sm text-gray-500"
          >
            로그아웃
          </button>
        </div>
      </header>

      {/* Profile Section */}
      <div className="bg-white p-6 border-b">
        <div className="flex items-center gap-4">
          <div className="w-20 h-20 bg-orange-100 rounded-full flex items-center justify-center">
            {profile?.profileImage ? (
              <img
                src={profile.profileImage}
                alt=""
                className="w-full h-full rounded-full object-cover"
              />
            ) : (
              <span className="text-3xl text-orange-500">
                {profile?.nickname?.charAt(0).toUpperCase()}
              </span>
            )}
          </div>
          <div className="flex-1">
            <h2 className="text-xl font-bold text-gray-900">{profile?.nickname}</h2>
            <p className="text-sm text-gray-500">{profile?.email}</p>
            <div className="flex gap-4 mt-2">
              <div className="text-center">
                <p className="text-lg font-semibold text-orange-500">
                  {profile?.rating?.toFixed(1) || '-'}
                </p>
                <p className="text-xs text-gray-500">평점</p>
              </div>
              <div className="text-center">
                <p className="text-lg font-semibold text-orange-500">
                  {profile?.totalMatches || 0}
                </p>
                <p className="text-xs text-gray-500">경기</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="bg-white border-b">
        <div className="flex">
          <button
            onClick={() => setActiveTab('participated')}
            className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors ${
              activeTab === 'participated'
                ? 'border-orange-500 text-orange-500'
                : 'border-transparent text-gray-500'
            }`}
          >
            참가한 경기 ({participatedMatches.length})
          </button>
          <button
            onClick={() => setActiveTab('hosted')}
            className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors ${
              activeTab === 'hosted'
                ? 'border-orange-500 text-orange-500'
                : 'border-transparent text-gray-500'
            }`}
          >
            개설한 경기 ({hostedMatches.length})
          </button>
        </div>
      </div>

      {/* Match List */}
      <div className="flex-1 overflow-y-auto">
        {activeTab === 'participated' ? (
          participatedMatches.length > 0 ? (
            <div className="p-4 space-y-3">
              {participatedMatches.map((match) => (
                <button
                  key={match.id}
                  onClick={() => router.push(`/matches/${match.id}`)}
                  className="w-full bg-white rounded-lg p-4 shadow-sm text-left"
                >
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="font-medium text-gray-900 flex-1 truncate pr-2">
                      {match.title}
                    </h3>
                    {getStatusBadge(getParticipationStatus(match.id))}
                  </div>
                  <div className="flex items-center gap-2 text-sm text-gray-500">
                    <CalendarIcon />
                    <span>{formatDate(match.matchDate)}</span>
                    <span className="text-gray-300">|</span>
                    <ClockIcon />
                    <span>{formatTime(match.startTime)}</span>
                  </div>
                  <div className="flex items-center gap-2 text-sm text-gray-500 mt-1">
                    <LocationIcon />
                    <span className="truncate">{match.address}</span>
                  </div>
                  <div className="flex justify-between items-center mt-2">
                    <span className="text-xs text-gray-400">호스트: {match.hostNickname}</span>
                    <span className="text-sm font-medium text-orange-500">
                      {match.currentParticipants}/{match.maxParticipants}
                    </span>
                  </div>
                </button>
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center h-64 text-gray-400">
              <p>참가한 경기가 없습니다</p>
              <button
                onClick={() => router.push('/home')}
                className="mt-2 text-orange-500 text-sm font-medium"
              >
                경기 찾기
              </button>
            </div>
          )
        ) : hostedMatches.length > 0 ? (
          <div className="p-4 space-y-3">
            {hostedMatches.map((match) => (
              <button
                key={match.id}
                onClick={() => router.push(`/matches/${match.id}`)}
                className="w-full bg-white rounded-lg p-4 shadow-sm text-left"
              >
                <div className="flex justify-between items-start mb-2">
                  <h3 className="font-medium text-gray-900 flex-1 truncate pr-2">
                    {match.title}
                  </h3>
                  {getStatusBadge(match.status)}
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-500">
                  <CalendarIcon />
                  <span>{formatDate(match.matchDate)}</span>
                  <span className="text-gray-300">|</span>
                  <ClockIcon />
                  <span>{formatTime(match.startTime)}</span>
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-500 mt-1">
                  <LocationIcon />
                  <span className="truncate">{match.address}</span>
                </div>
                <div className="flex justify-end mt-2">
                  <span className="text-sm font-medium text-orange-500">
                    {match.currentParticipants}/{match.maxParticipants}
                  </span>
                </div>
              </button>
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center h-64 text-gray-400">
            <p>개설한 경기가 없습니다</p>
            <button
              onClick={() => router.push('/matches/new')}
              className="mt-2 text-orange-500 text-sm font-medium"
            >
              경기 만들기
            </button>
          </div>
        )}
      </div>

      {/* Bottom Navigation */}
      <nav className="bg-white border-t border-gray-200 z-10 flex-shrink-0">
        <div className="flex justify-around py-2">
          <button
            onClick={() => router.push('/home')}
            className="flex flex-col items-center py-1 px-3 text-gray-400"
          >
            <HomeIcon />
            <span className="text-xs mt-1">홈</span>
          </button>
          <button
            onClick={() => router.push('/matches/new')}
            className="flex flex-col items-center py-1 px-3 text-gray-400"
          >
            <PlusIcon />
            <span className="text-xs mt-1">경기 생성</span>
          </button>
          <button className="flex flex-col items-center py-1 px-3 text-orange-500">
            <UserIcon />
            <span className="text-xs mt-1">마이</span>
          </button>
        </div>
      </nav>
    </div>
  );
}

function CalendarIcon() {
  return (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
    </svg>
  );
}

function ClockIcon() {
  return (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  );
}

function LocationIcon() {
  return (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
      <path strokeLinecap="round" strokeLinejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
    </svg>
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
