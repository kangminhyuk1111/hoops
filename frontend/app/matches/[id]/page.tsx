'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter, useParams } from 'next/navigation';
import toast from 'react-hot-toast';
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
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [rejectingParticipantId, setRejectingParticipantId] = useState<number | null>(null);

  // Check if current user is participant
  const myParticipation = participants.find(p => p.userId === user?.id);
  const isHost = match?.hostId === user?.id;
  const isParticipant = !!myParticipation;
  const isPending = myParticipation?.status === 'PENDING';
  const isConfirmed = myParticipation?.status === 'CONFIRMED';
  const isRejected = myParticipation?.status === 'REJECTED';

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
      setError('경기 정보를 불러오는데 실패했습니다');
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

  const handleJoinClick = () => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }
    setShowJoinModal(true);
  };

  const handleJoinConfirm = async () => {
    setActionLoading(true);
    try {
      await api.post(`/api/matches/${matchId}/participations`);
      toast.success('참가 신청이 완료되었습니다');
      setShowJoinModal(false);
      fetchParticipants();
    } catch (err: any) {
      toast.error(err.response?.data?.message || '참가 신청에 실패했습니다');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!myParticipation) return;

    setActionLoading(true);
    try {
      await api.delete(`/api/matches/${matchId}/participations/${myParticipation.id}`);
      toast.success('참가가 취소되었습니다');
      fetchParticipants();
    } catch (err: any) {
      toast.error(err.response?.data?.message || '참가 취소에 실패했습니다');
    } finally {
      setActionLoading(false);
    }
  };

  const handleApprove = async (participationId: number) => {
    setActionLoading(true);
    try {
      await api.put(`/api/matches/${matchId}/participations/${participationId}/approve`);
      toast.success('참가를 승인했습니다');
      fetchParticipants();
    } catch (err: any) {
      toast.error(err.response?.data?.message || '승인에 실패했습니다');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRejectClick = (participationId: number) => {
    setRejectingParticipantId(participationId);
    setShowRejectModal(true);
  };

  const handleRejectConfirm = async () => {
    if (!rejectingParticipantId || !rejectReason.trim()) {
      toast.error('거절 사유를 입력해주세요');
      return;
    }

    setActionLoading(true);
    try {
      await api.put(`/api/matches/${matchId}/participations/${rejectingParticipantId}/reject`, {
        reason: rejectReason
      });
      toast.success('참가를 거절했습니다');
      setShowRejectModal(false);
      setRejectReason('');
      setRejectingParticipantId(null);
      fetchParticipants();
    } catch (err: any) {
      toast.error(err.response?.data?.message || '거절에 실패했습니다');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancelMatch = async () => {
    if (!cancelReason.trim()) {
      toast.error('취소 사유를 입력해주세요');
      return;
    }

    setActionLoading(true);
    try {
      await api.delete(`/api/matches/${matchId}`, {
        data: { reason: cancelReason }
      });
      toast.success('경기가 취소되었습니다');
      setShowCancelModal(false);
      setCancelReason('');
      router.push('/');
    } catch (err: any) {
      toast.error(err.response?.data?.message || '경기 취소에 실패했습니다');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReactivateMatch = async () => {
    if (!confirm('경기를 다시 활성화하시겠습니까?')) return;

    setActionLoading(true);
    try {
      await api.post(`/api/matches/${matchId}/reactivate`);
      toast.success('경기가 복구되었습니다');
      fetchMatchDetail();
      fetchParticipants();
    } catch (err: any) {
      toast.error(err.response?.data?.message || '경기 복구에 실패했습니다');
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
        return <span className="px-2 py-0.5 bg-yellow-100 text-yellow-700 text-xs rounded-full">대기중</span>;
      case 'CONFIRMED':
        return <span className="px-2 py-0.5 bg-green-100 text-green-700 text-xs rounded-full">확정</span>;
      case 'REJECTED':
        return <span className="px-2 py-0.5 bg-red-100 text-red-700 text-xs rounded-full">거절됨</span>;
      case 'CANCELLED':
        return <span className="px-2 py-0.5 bg-gray-100 text-gray-700 text-xs rounded-full">취소됨</span>;
      default:
        return null;
    }
  };

  const getMatchStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <span className="px-2.5 py-1 bg-blue-100 text-blue-700 text-xs font-medium rounded-full">모집중</span>;
      case 'CONFIRMED':
        return <span className="px-2.5 py-1 bg-green-100 text-green-700 text-xs font-medium rounded-full">확정</span>;
      case 'IN_PROGRESS':
        return <span className="px-2.5 py-1 bg-orange-100 text-orange-700 text-xs font-medium rounded-full">진행중</span>;
      case 'ENDED':
        return <span className="px-2.5 py-1 bg-gray-100 text-gray-700 text-xs font-medium rounded-full">종료</span>;
      case 'CANCELLED':
        return <span className="px-2.5 py-1 bg-red-100 text-red-700 text-xs font-medium rounded-full">취소됨</span>;
      case 'FULL':
        return <span className="px-2.5 py-1 bg-purple-100 text-purple-700 text-xs font-medium rounded-full">마감</span>;
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
          <p className="text-gray-500 mb-4">{error || '경기를 찾을 수 없습니다'}</p>
          <button onClick={() => router.back()} className="text-orange-500 font-medium">
            돌아가기
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
              <p className="text-sm text-gray-500 mt-1">호스트: {match.hostNickname}</p>
            </div>
            <div className="text-right">
              <p className="text-2xl font-bold text-orange-500">
                {match.currentParticipants}<span className="text-gray-400 text-lg">/{match.maxParticipants}</span>
              </p>
              <p className="text-xs text-gray-500">참가자</p>
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
            참가자 ({confirmedParticipants.length}/{match.maxParticipants})
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
                        평점: {participant.rating?.toFixed(1) || '-'} | 경기: {participant.totalMatches || 0}
                      </p>
                    </div>
                  </div>
                  {getStatusBadge(participant.status)}
                </div>
              ))}
            </div>
          ) : (
            <p className="text-gray-400 text-sm text-center py-4">확정된 참가자가 없습니다</p>
          )}
        </div>

        {/* Pending Requests (Host Only) */}
        {isHost && pendingParticipants.length > 0 && (
          <div className="bg-white mt-2 p-4">
            <h3 className="font-semibold text-gray-900 mb-3">
              대기 중인 요청 ({pendingParticipants.length})
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
                        평점: {participant.rating?.toFixed(1) || '-'} | 경기: {participant.totalMatches || 0}
                      </p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleApprove(participant.id)}
                      disabled={actionLoading}
                      className="px-3 py-1.5 bg-green-500 text-white text-xs font-medium rounded-lg disabled:opacity-50"
                    >
                      승인
                    </button>
                    <button
                      onClick={() => handleRejectClick(participant.id)}
                      disabled={actionLoading}
                      className="px-3 py-1.5 bg-red-500 text-white text-xs font-medium rounded-lg disabled:opacity-50"
                    >
                      거절
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
                  이 경기가 취소되었습니다. 1시간 이내에 복구할 수 있습니다.
                </p>
                <button
                  onClick={handleReactivateMatch}
                  disabled={actionLoading}
                  className="w-full py-3 bg-orange-500 text-white rounded-lg font-medium disabled:opacity-50"
                >
                  {actionLoading ? '처리 중...' : '경기 복구'}
                </button>
              </div>
            ) : match.status !== 'ENDED' && match.status !== 'IN_PROGRESS' ? (
              <button
                onClick={() => setShowCancelModal(true)}
                disabled={actionLoading}
                className="w-full py-3 border border-red-500 text-red-500 rounded-lg font-medium disabled:opacity-50"
              >
                경기 취소
              </button>
            ) : null}
          </div>
        )}
      </div>

      {/* Bottom Action Button */}
      {!isHost && match.status === 'PENDING' && (
        <div className="bg-white border-t p-4">
          {!isParticipant ? (
            <button
              onClick={handleJoinClick}
              disabled={actionLoading || match.currentParticipants >= match.maxParticipants}
              className="w-full py-3.5 bg-orange-500 text-white rounded-lg font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              {actionLoading ? '처리 중...' : match.currentParticipants >= match.maxParticipants ? '마감' : '참가 신청'}
            </button>
          ) : isPending ? (
            <div className="space-y-2">
              <p className="text-center text-yellow-600 text-sm font-medium">호스트 승인 대기 중</p>
              <button
                onClick={handleCancel}
                disabled={actionLoading}
                className="w-full py-3.5 border border-gray-300 text-gray-700 rounded-lg font-medium disabled:opacity-50"
              >
                신청 취소
              </button>
            </div>
          ) : isConfirmed ? (
            <div className="space-y-2">
              <p className="text-center text-green-600 text-sm font-medium">참가 확정!</p>
              <button
                onClick={handleCancel}
                disabled={actionLoading}
                className="w-full py-3.5 border border-red-500 text-red-500 rounded-lg font-medium disabled:opacity-50"
              >
                참가 취소
              </button>
            </div>
          ) : isRejected ? (
            <div className="py-2">
              <p className="text-center text-red-500 text-sm font-medium">참가 신청이 거절되었습니다</p>
            </div>
          ) : null}
        </div>
      )}

      {/* Cancel Match Modal */}
      {showCancelModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl w-full max-w-sm p-5">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">경기 취소</h3>
            <p className="text-sm text-gray-500 mb-4">취소 사유를 입력해주세요.</p>
            <textarea
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="예: 개인 사정으로 인해 취소합니다"
              className="w-full p-3 border border-gray-300 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-orange-500"
              rows={3}
            />
            <div className="flex gap-3 mt-4">
              <button
                onClick={() => {
                  setShowCancelModal(false);
                  setCancelReason('');
                }}
                className="flex-1 py-2.5 border border-gray-300 text-gray-700 rounded-lg font-medium"
              >
                취소
              </button>
              <button
                onClick={handleCancelMatch}
                disabled={actionLoading || !cancelReason.trim()}
                className="flex-1 py-2.5 bg-red-500 text-white rounded-lg font-medium disabled:opacity-50"
              >
                {actionLoading ? '처리 중...' : '경기 취소'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Join Confirmation Modal */}
      {showJoinModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl w-full max-w-sm p-5">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">참가 신청</h3>
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 mb-4">
              <p className="text-sm text-yellow-800 font-medium mb-1">주의사항</p>
              <ul className="text-xs text-yellow-700 space-y-1">
                <li>- 호스트가 거절하면 재신청이 불가능합니다.</li>
                <li>- 신청 취소 후에도 1회만 재신청할 수 있습니다.</li>
                <li>- 신중하게 결정해주세요.</li>
              </ul>
            </div>
            <p className="text-sm text-gray-600 mb-4">
              <span className="font-medium">{match?.title}</span> 경기에 참가 신청하시겠습니까?
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowJoinModal(false)}
                className="flex-1 py-2.5 border border-gray-300 text-gray-700 rounded-lg font-medium"
              >
                취소
              </button>
              <button
                onClick={handleJoinConfirm}
                disabled={actionLoading}
                className="flex-1 py-2.5 bg-orange-500 text-white rounded-lg font-medium disabled:opacity-50"
              >
                {actionLoading ? '처리 중...' : '신청하기'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Reject Confirmation Modal */}
      {showRejectModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl w-full max-w-sm p-5">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">참가 거절</h3>
            <p className="text-sm text-gray-500 mb-4">거절 사유를 입력해주세요.</p>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="예: 정원이 초과되었습니다"
              className="w-full p-3 border border-gray-300 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-red-500"
              rows={3}
            />
            <div className="flex gap-3 mt-4">
              <button
                onClick={() => {
                  setShowRejectModal(false);
                  setRejectReason('');
                  setRejectingParticipantId(null);
                }}
                className="flex-1 py-2.5 border border-gray-300 text-gray-700 rounded-lg font-medium"
              >
                취소
              </button>
              <button
                onClick={handleRejectConfirm}
                disabled={actionLoading || !rejectReason.trim()}
                className="flex-1 py-2.5 bg-red-500 text-white rounded-lg font-medium disabled:opacity-50"
              >
                {actionLoading ? '처리 중...' : '거절하기'}
              </button>
            </div>
          </div>
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
