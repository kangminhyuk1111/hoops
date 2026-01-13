'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useAuthStore } from '@/lib/store/auth';
import api from '@/lib/api';
import { Match, ParticipantDetail, ParticipationStatus } from '@/types';

declare global {
  interface Window {
    kakao: any;
  }
}

export default function MatchDetailPage() {
  const router = useRouter();
  const params = useParams();
  const matchId = params.id as string;
  const { isAuthenticated, user } = useAuthStore();
  const mapRef = useRef<HTMLDivElement>(null);

  const [match, setMatch] = useState<Match | null>(null);
  const [participants, setParticipants] = useState<ParticipantDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isMapLoaded, setIsMapLoaded] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelReason, setCancelReason] = useState('');

  // Check if current user is participant
  const myParticipation = participants.find(p => p.userId === user?.id);
  const isHost = match?.hostId === user?.id;
  const isParticipant = !!myParticipation;
  const isPending = myParticipation?.status === 'PENDING';
  const isConfirmed = myParticipation?.status === 'CONFIRMED';

  // Check if cancellation is allowed (2 hours before match start)
  const canCancelByTime = (): boolean => {
    if (!match) return false;
    const matchStart = new Date(`${match.matchDate}T${match.startTime}`);
    const now = new Date();
    const twoHoursInMs = 2 * 60 * 60 * 1000;
    return matchStart.getTime() - now.getTime() > twoHoursInMs;
  };

  useEffect(() => {
    fetchMatchDetail();
    fetchParticipants();
  }, [matchId]);

  // Load Kakao Map SDK
  useEffect(() => {
    const script = document.getElementById('kakao-map-script');

    if (!script) {
      const newScript = document.createElement('script');
      newScript.id = 'kakao-map-script';
      newScript.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NEXT_PUBLIC_KAKAO_JS_KEY}&autoload=false&libraries=services`;
      newScript.async = true;
      newScript.onload = () => {
        window.kakao.maps.load(() => {
          setIsMapLoaded(true);
        });
      };
      document.head.appendChild(newScript);
    } else if (window.kakao && window.kakao.maps) {
      setIsMapLoaded(true);
    }
  }, []);

  // Initialize map
  useEffect(() => {
    if (!isMapLoaded || !mapRef.current || !match) return;

    const options = {
      center: new window.kakao.maps.LatLng(match.latitude, match.longitude),
      level: 3,
    };

    const map = new window.kakao.maps.Map(mapRef.current, options);

    const marker = new window.kakao.maps.Marker({
      position: new window.kakao.maps.LatLng(match.latitude, match.longitude),
      map,
    });

    const infowindow = new window.kakao.maps.InfoWindow({
      content: `<div style="padding:8px 12px;font-size:13px;">${match.address}</div>`,
    });

    window.kakao.maps.event.addListener(marker, 'click', () => {
      infowindow.open(map, marker);
    });
  }, [isMapLoaded, match]);

  const fetchMatchDetail = async () => {
    try {
      const response = await api.get(`/api/matches/${matchId}`);
      setMatch(response.data);
    } catch (err) {
      console.error('Failed to fetch match:', err);
      setError('Failed to load match details');
    } finally {
      setLoading(false);
    }
  };

  const fetchParticipants = async () => {
    try {
      const response = await api.get(`/api/matches/${matchId}/participants`);
      setParticipants(response.data);
    } catch (err) {
      console.error('Failed to fetch participants:', err);
    }
  };

  const handleJoin = async () => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    setActionLoading(true);
    try {
      await api.post(`/api/matches/${matchId}/participations`);
      fetchParticipants();
    } catch (err: any) {
      const errorCode = err.response?.data?.errorCode;
      const message = getErrorMessage(errorCode, err.response?.data?.message);
      alert(message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!myParticipation) return;

    if (!canCancelByTime()) {
      alert('경기 시작 2시간 전까지만 참가 취소가 가능합니다.');
      return;
    }

    setActionLoading(true);
    try {
      await api.delete(`/api/matches/${matchId}/participations/${myParticipation.id}`);
      fetchParticipants();
    } catch (err: any) {
      const errorCode = err.response?.data?.errorCode;
      const message = getErrorMessage(errorCode, err.response?.data?.message);
      alert(message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleApprove = async (participationId: number) => {
    setActionLoading(true);
    try {
      await api.put(`/api/matches/${matchId}/participations/${participationId}/approve`);
      fetchParticipants();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to approve');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async (participationId: number) => {
    setActionLoading(true);
    try {
      await api.put(`/api/matches/${matchId}/participations/${participationId}/reject`);
      fetchParticipants();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to reject');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancelMatch = async () => {
    if (!cancelReason.trim()) {
      alert('취소 사유를 입력해주세요.');
      return;
    }

    setActionLoading(true);
    try {
      await api.delete(`/api/matches/${matchId}`, {
        data: { reason: cancelReason }
      });
      setShowCancelModal(false);
      setCancelReason('');
      router.push('/');
    } catch (err: any) {
      const errorCode = err.response?.data?.errorCode;
      const message = getErrorMessage(errorCode, err.response?.data?.message);
      alert(message);
    } finally {
      setActionLoading(false);
    }
  };

  const getErrorMessage = (errorCode: string | undefined, defaultMessage: string): string => {
    switch (errorCode) {
      case 'CANCEL_TIME_EXCEEDED':
        return '경기 시작 2시간 전까지만 취소 가능합니다.';
      case 'CANCEL_REASON_REQUIRED':
        return '취소 사유를 입력해주세요.';
      case 'OVERLAPPING_PARTICIPATION':
        return '해당 시간에 이미 참가 신청한 경기가 있습니다.';
      case 'OVERLAPPING_HOSTING':
        return '해당 시간에 이미 생성한 경기가 있습니다.';
      case 'MATCH_TOO_SOON':
        return '경기는 최소 2시간 후부터 생성 가능합니다.';
      case 'MATCH_TOO_FAR':
        return '경기는 14일 이내로만 생성 가능합니다.';
      case 'PARTICIPATION_CANCEL_TIME_EXCEEDED':
        return '경기 시작 2시간 전까지만 참가 취소가 가능합니다.';
      default:
        return defaultMessage || '오류가 발생했습니다.';
    }
  };

  const handleReactivateMatch = async () => {
    if (!confirm('Are you sure you want to reactivate this match?')) return;

    setActionLoading(true);
    try {
      await api.post(`/api/matches/${matchId}/reactivate`);
      fetchMatchDetail();
      fetchParticipants();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to reactivate match');
    } finally {
      setActionLoading(false);
    }
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      weekday: 'short',
    });
  };

  const formatTime = (timeStr: string) => {
    return timeStr.slice(0, 5);
  };

  const getStatusBadge = (status: ParticipationStatus) => {
    switch (status) {
      case 'PENDING':
        return <span className="px-2 py-0.5 bg-yellow-100 text-yellow-700 text-xs rounded-full">Pending</span>;
      case 'CONFIRMED':
        return <span className="px-2 py-0.5 bg-green-100 text-green-700 text-xs rounded-full">Confirmed</span>;
      case 'REJECTED':
        return <span className="px-2 py-0.5 bg-red-100 text-red-700 text-xs rounded-full">Rejected</span>;
      case 'CANCELLED':
        return <span className="px-2 py-0.5 bg-gray-100 text-gray-700 text-xs rounded-full">Cancelled</span>;
      default:
        return null;
    }
  };

  const getMatchStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <span className="px-2.5 py-1 bg-blue-100 text-blue-700 text-xs font-medium rounded-full">Recruiting</span>;
      case 'CONFIRMED':
        return <span className="px-2.5 py-1 bg-green-100 text-green-700 text-xs font-medium rounded-full">Confirmed</span>;
      case 'IN_PROGRESS':
        return <span className="px-2.5 py-1 bg-orange-100 text-orange-700 text-xs font-medium rounded-full">In Progress</span>;
      case 'ENDED':
        return <span className="px-2.5 py-1 bg-gray-100 text-gray-700 text-xs font-medium rounded-full">Ended</span>;
      case 'CANCELLED':
        return <span className="px-2.5 py-1 bg-red-100 text-red-700 text-xs font-medium rounded-full">Cancelled</span>;
      case 'FULL':
        return <span className="px-2.5 py-1 bg-purple-100 text-purple-700 text-xs font-medium rounded-full">Full</span>;
      default:
        return null;
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-2 border-orange-500 border-t-transparent"></div>
      </div>
    );
  }

  if (error || !match) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-500 mb-4">{error || 'Match not found'}</p>
          <button onClick={() => router.back()} className="text-orange-500 font-medium">
            Go Back
          </button>
        </div>
      </div>
    );
  }

  const confirmedParticipants = participants.filter(p => p.status === 'CONFIRMED');
  const pendingParticipants = participants.filter(p => p.status === 'PENDING');

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-20">
        <div className="px-4 py-3 flex items-center gap-3">
          <button onClick={() => router.back()} className="text-gray-600">
            <BackIcon />
          </button>
          <h1 className="text-lg font-semibold text-gray-800 flex-1 truncate">{match.title}</h1>
          {getMatchStatusBadge(match.status)}
        </div>
      </header>

      {/* Content */}
      <div className="flex-1 overflow-y-auto">
        {/* Map */}
        <div className="h-[180px] relative bg-gray-100">
          {isMapLoaded ? (
            <div ref={mapRef} className="absolute inset-0" />
          ) : (
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="animate-spin rounded-full h-6 w-6 border-2 border-orange-500 border-t-transparent"></div>
            </div>
          )}
        </div>

        {/* Match Info */}
        <div className="bg-white p-4 border-b">
          <div className="flex items-start justify-between mb-3">
            <div>
              <h2 className="text-xl font-bold text-gray-900">{match.title}</h2>
              <p className="text-sm text-gray-500 mt-1">Host: {match.hostNickname}</p>
            </div>
            <div className="text-right">
              <p className="text-2xl font-bold text-orange-500">
                {match.currentParticipants}<span className="text-gray-400 text-lg">/{match.maxParticipants}</span>
              </p>
              <p className="text-xs text-gray-500">participants</p>
            </div>
          </div>

          {match.description && (
            <p className="text-gray-600 text-sm mb-4">{match.description}</p>
          )}

          <div className="space-y-2">
            <div className="flex items-center gap-3 text-sm">
              <CalendarIcon />
              <span className="text-gray-700">{formatDate(match.matchDate)}</span>
            </div>
            <div className="flex items-center gap-3 text-sm">
              <ClockIcon />
              <span className="text-gray-700">{formatTime(match.startTime)} - {formatTime(match.endTime)}</span>
            </div>
            <div className="flex items-center gap-3 text-sm">
              <LocationIcon />
              <span className="text-gray-700">{match.address}</span>
            </div>
          </div>
        </div>

        {/* Participants Section */}
        <div className="bg-white mt-2 p-4">
          <h3 className="font-semibold text-gray-900 mb-3">
            Participants ({confirmedParticipants.length}/{match.maxParticipants})
          </h3>

          {confirmedParticipants.length > 0 ? (
            <div className="space-y-2">
              {confirmedParticipants.map((participant) => (
                <div key={participant.id} className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center">
                      {participant.profileImage ? (
                        <img src={participant.profileImage} alt="" className="w-full h-full rounded-full object-cover" />
                      ) : (
                        <UserIcon />
                      )}
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{participant.nickname}</p>
                      <p className="text-xs text-gray-500">
                        Rating: {participant.rating?.toFixed(1) || '-'} | Matches: {participant.totalMatches || 0}
                      </p>
                    </div>
                  </div>
                  {getStatusBadge(participant.status)}
                </div>
              ))}
            </div>
          ) : (
            <p className="text-gray-400 text-sm text-center py-4">No confirmed participants yet</p>
          )}
        </div>

        {/* Pending Requests (Host Only) */}
        {isHost && pendingParticipants.length > 0 && (
          <div className="bg-white mt-2 p-4">
            <h3 className="font-semibold text-gray-900 mb-3">
              Pending Requests ({pendingParticipants.length})
            </h3>
            <div className="space-y-2">
              {pendingParticipants.map((participant) => (
                <div key={participant.id} className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center">
                      {participant.profileImage ? (
                        <img src={participant.profileImage} alt="" className="w-full h-full rounded-full object-cover" />
                      ) : (
                        <UserIcon />
                      )}
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{participant.nickname}</p>
                      <p className="text-xs text-gray-500">
                        Rating: {participant.rating?.toFixed(1) || '-'} | Matches: {participant.totalMatches || 0}
                      </p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleApprove(participant.id)}
                      disabled={actionLoading}
                      className="px-3 py-1.5 bg-green-500 text-white text-xs font-medium rounded-lg disabled:opacity-50"
                    >
                      Approve
                    </button>
                    <button
                      onClick={() => handleReject(participant.id)}
                      disabled={actionLoading}
                      className="px-3 py-1.5 bg-red-500 text-white text-xs font-medium rounded-lg disabled:opacity-50"
                    >
                      Reject
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Host Actions */}
        {isHost && (
          <div className="bg-white mt-2 p-4">
            {match.status === 'CANCELLED' ? (
              <div className="space-y-3">
                <p className="text-center text-gray-500 text-sm">
                  This match was cancelled. You can reactivate it within 1 hour.
                </p>
                <button
                  onClick={handleReactivateMatch}
                  disabled={actionLoading}
                  className="w-full py-3 bg-orange-500 text-white rounded-lg font-medium disabled:opacity-50"
                >
                  {actionLoading ? 'Processing...' : 'Reactivate Match'}
                </button>
              </div>
            ) : match.status !== 'ENDED' && match.status !== 'IN_PROGRESS' ? (
              <div className="space-y-2">
                {!canCancelByTime() && (
                  <p className="text-center text-yellow-600 text-xs">
                    경기 시작 2시간 전까지만 취소 가능합니다
                  </p>
                )}
                <button
                  onClick={() => setShowCancelModal(true)}
                  disabled={actionLoading || !canCancelByTime()}
                  className="w-full py-3 border border-red-500 text-red-500 rounded-lg font-medium disabled:opacity-50 disabled:border-gray-300 disabled:text-gray-400"
                >
                  Cancel Match
                </button>
              </div>
            ) : null}
          </div>
        )}

        {/* Cancel Match Modal */}
        {showCancelModal && (
          <div className="fixed inset-0 bg-black/50 z-30 flex items-center justify-center p-4">
            <div className="bg-white w-full max-w-sm rounded-2xl overflow-hidden">
              <div className="p-4 border-b border-gray-100">
                <h3 className="font-semibold text-gray-900 text-lg">경기 취소</h3>
              </div>
              <div className="p-4 space-y-4">
                <p className="text-sm text-gray-600">
                  경기를 취소하시겠습니까? 취소 사유를 입력해주세요.
                </p>
                <textarea
                  value={cancelReason}
                  onChange={(e) => setCancelReason(e.target.value)}
                  placeholder="취소 사유를 입력하세요..."
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg text-base text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-orange-500 h-24 resize-none"
                  maxLength={500}
                />
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => {
                      setShowCancelModal(false);
                      setCancelReason('');
                    }}
                    className="flex-1 py-3 border border-gray-300 text-gray-700 rounded-lg font-medium"
                  >
                    취소
                  </button>
                  <button
                    type="button"
                    onClick={handleCancelMatch}
                    disabled={actionLoading || !cancelReason.trim()}
                    className="flex-1 py-3 bg-red-500 text-white rounded-lg font-medium disabled:bg-gray-300"
                  >
                    {actionLoading ? '처리 중...' : '경기 취소'}
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Bottom Action Button */}
      {!isHost && match.status === 'PENDING' && (
        <div className="bg-white border-t p-4">
          {!isParticipant ? (
            <button
              onClick={handleJoin}
              disabled={actionLoading || match.currentParticipants >= match.maxParticipants}
              className="w-full py-3.5 bg-orange-500 text-white rounded-lg font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              {actionLoading ? 'Processing...' : match.currentParticipants >= match.maxParticipants ? 'Full' : 'Join Match'}
            </button>
          ) : isPending ? (
            <div className="space-y-2">
              <p className="text-center text-yellow-600 text-sm font-medium">Waiting for host approval</p>
              {!canCancelByTime() && (
                <p className="text-center text-yellow-600 text-xs">
                  경기 시작 2시간 전까지만 취소 가능합니다
                </p>
              )}
              <button
                onClick={handleCancel}
                disabled={actionLoading || !canCancelByTime()}
                className="w-full py-3.5 border border-gray-300 text-gray-700 rounded-lg font-medium disabled:opacity-50"
              >
                Cancel Request
              </button>
            </div>
          ) : isConfirmed ? (
            <div className="space-y-2">
              <p className="text-center text-green-600 text-sm font-medium">You're in!</p>
              {!canCancelByTime() && (
                <p className="text-center text-yellow-600 text-xs">
                  경기 시작 2시간 전까지만 취소 가능합니다
                </p>
              )}
              <button
                onClick={handleCancel}
                disabled={actionLoading || !canCancelByTime()}
                className="w-full py-3.5 border border-red-500 text-red-500 rounded-lg font-medium disabled:opacity-50 disabled:border-gray-300 disabled:text-gray-400"
              >
                Leave Match
              </button>
            </div>
          ) : null}
        </div>
      )}
    </div>
  );
}

function BackIcon() {
  return (
    <svg className="w-6 h-6" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
    </svg>
  );
}

function CalendarIcon() {
  return (
    <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
    </svg>
  );
}

function ClockIcon() {
  return (
    <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  );
}

function LocationIcon() {
  return (
    <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
      <path strokeLinecap="round" strokeLinejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
    </svg>
  );
}

function UserIcon() {
  return (
    <svg className="w-5 h-5 text-gray-400" fill="currentColor" viewBox="0 0 24 24">
      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
    </svg>
  );
}
