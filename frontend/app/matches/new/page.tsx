'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/store/auth';
import api from '@/lib/api';
import { Location, CreateMatchRequest } from '@/types';

export default function CreateMatchPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuthStore();

  // Form state
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [matchDate, setMatchDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [maxParticipants, setMaxParticipants] = useState(10);

  // Location state
  const [locationId, setLocationId] = useState<number | null>(null);
  const [locationName, setLocationName] = useState('');
  const [locationAddress, setLocationAddress] = useState('');

  // UI state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showLocationPicker, setShowLocationPicker] = useState(false);
  const [existingLocations, setExistingLocations] = useState<Location[]>([]);
  const [searchKeyword, setSearchKeyword] = useState('');

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, router]);

  useEffect(() => {
    fetchLocations();
  }, []);

  const fetchLocations = async () => {
    try {
      const response = await api.get('/api/locations');
      setExistingLocations(response.data);
    } catch (err) {
      console.error('장소 목록 로드 실패:', err);
    }
  };

  const searchLocations = async () => {
    if (!searchKeyword.trim()) {
      fetchLocations();
      return;
    }
    try {
      const response = await api.get('/api/locations/search', {
        params: { keyword: searchKeyword }
      });
      setExistingLocations(response.data);
    } catch (err) {
      console.error('장소 검색 실패:', err);
    }
  };

  const selectLocation = (location: Location) => {
    setLocationId(location.id);
    setLocationName(location.name);
    setLocationAddress(location.address);
    setShowLocationPicker(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!locationId) {
      setError('장소를 선택해주세요.');
      return;
    }

    if (!validateDuration()) {
      setError('경기 시간은 최소 1시간 이상이어야 합니다.');
      return;
    }

    setLoading(true);

    try {
      const matchRequest: CreateMatchRequest = {
        locationId,
        title,
        description: description || undefined,
        matchDate,
        startTime,
        endTime,
        maxParticipants,
      };

      const response = await api.post('/api/matches', matchRequest);
      router.push(`/matches/${response.data.id}`);
    } catch (err: any) {
      console.error('경기 생성 실패:', err);
      const errorCode = err.response?.data?.errorCode;
      setError(getErrorMessage(errorCode, err.response?.data?.message));
    } finally {
      setLoading(false);
    }
  };

  const today = new Date().toISOString().split('T')[0];
  const maxDate = new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

  // Validate minimum 1-hour duration
  const validateDuration = (): boolean => {
    if (!startTime || !endTime) return true;
    const [startH, startM] = startTime.split(':').map(Number);
    const [endH, endM] = endTime.split(':').map(Number);
    const startMinutes = startH * 60 + startM;
    const endMinutes = endH * 60 + endM;
    return endMinutes - startMinutes >= 60;
  };

  // Get error message for API error codes
  const getErrorMessage = (errorCode: string | undefined, defaultMessage: string): string => {
    switch (errorCode) {
      case 'OVERLAPPING_HOSTING':
        return '해당 시간에 이미 생성한 경기가 있습니다.';
      case 'MATCH_TOO_SOON':
        return '경기는 최소 2시간 후부터 생성 가능합니다.';
      case 'MATCH_TOO_FAR':
        return '경기는 14일 이내로만 생성 가능합니다.';
      case 'MATCH_DURATION_TOO_SHORT':
        return '경기 시간은 최소 1시간 이상이어야 합니다.';
      default:
        return defaultMessage || '오류가 발생했습니다.';
    }
  };

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-20">
        <div className="px-4 py-3 flex items-center gap-3">
          <button onClick={() => router.back()} className="text-gray-600">
            <BackIcon />
          </button>
          <h1 className="text-lg font-semibold text-gray-800">경기 생성</h1>
        </div>
      </header>

      {/* Form */}
      <form onSubmit={handleSubmit} className="flex-1 p-4 space-y-5">
        {error && (
          <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm">
            {error}
          </div>
        )}

        {/* 장소 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">
            장소 *
          </label>
          {locationId ? (
            <div className="bg-orange-50 border border-orange-200 p-4 rounded-lg">
              <div className="flex justify-between items-start">
                <div>
                  <p className="font-medium text-gray-900">{locationName}</p>
                  <p className="text-sm text-gray-600 mt-0.5">{locationAddress}</p>
                </div>
                <button
                  type="button"
                  onClick={() => {
                    setLocationId(null);
                    setLocationName('');
                    setLocationAddress('');
                  }}
                  className="text-orange-500 text-sm font-medium"
                >
                  변경
                </button>
              </div>
            </div>
          ) : (
            <button
              type="button"
              onClick={() => setShowLocationPicker(true)}
              className="w-full px-4 py-4 border-2 border-dashed border-gray-300 rounded-lg text-gray-500 active:bg-gray-50"
            >
              장소를 선택하세요
            </button>
          )}
        </div>

        {/* 제목 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">
            경기 제목 *
          </label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="예: 주말 농구 한 판"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg text-base text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-orange-500"
            required
            maxLength={200}
          />
        </div>

        {/* 설명 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">
            설명
          </label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="경기에 대한 추가 정보를 입력하세요"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg text-base text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-orange-500 h-24 resize-none"
            maxLength={5000}
          />
        </div>

        {/* 날짜 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">
            경기 날짜 *
          </label>
          <input
            type="date"
            value={matchDate}
            onChange={(e) => setMatchDate(e.target.value)}
            min={today}
            max={maxDate}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg text-base text-gray-900 focus:outline-none focus:ring-2 focus:ring-orange-500"
            required
          />
          <p className="text-xs text-gray-400 mt-1">14일 이내의 날짜만 선택 가능합니다</p>
        </div>

        {/* 시간 */}
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              시작 시간 *
            </label>
            <input
              type="time"
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg text-base text-gray-900 focus:outline-none focus:ring-2 focus:ring-orange-500"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              종료 시간 *
            </label>
            <input
              type="time"
              value={endTime}
              onChange={(e) => setEndTime(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg text-base text-gray-900 focus:outline-none focus:ring-2 focus:ring-orange-500"
              required
            />
          </div>
        </div>
        {startTime && endTime && !validateDuration() && (
          <p className="text-xs text-red-500 -mt-3">경기 시간은 최소 1시간 이상이어야 합니다</p>
        )}

        {/* 최대 인원 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">
            최대 참가 인원 *
          </label>
          <div className="flex items-center gap-4">
            <button
              type="button"
              onClick={() => setMaxParticipants(Math.max(4, maxParticipants - 1))}
              className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center text-gray-700 text-xl font-medium active:bg-gray-200"
            >
              -
            </button>
            <span className="text-2xl font-semibold text-gray-900 w-16 text-center">
              {maxParticipants}
            </span>
            <button
              type="button"
              onClick={() => setMaxParticipants(Math.min(20, maxParticipants + 1))}
              className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center text-gray-700 text-xl font-medium active:bg-gray-200"
            >
              +
            </button>
            <span className="text-sm text-gray-500">명</span>
          </div>
          <p className="text-xs text-gray-400 mt-2">최소 4명 ~ 최대 20명</p>
        </div>

        {/* 장소 선택 모달 */}
        {showLocationPicker && (
          <div className="fixed inset-0 bg-black/50 z-30 flex items-end">
            <div className="bg-white w-full max-h-[80vh] rounded-t-2xl overflow-hidden">
              <div className="p-4 border-b border-gray-100">
                <div className="flex justify-between items-center mb-3">
                  <h3 className="font-semibold text-gray-900 text-lg">장소 선택</h3>
                  <button
                    type="button"
                    onClick={() => setShowLocationPicker(false)}
                    className="text-gray-500 text-sm"
                  >
                    닫기
                  </button>
                </div>
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={searchKeyword}
                    onChange={(e) => setSearchKeyword(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && searchLocations()}
                    placeholder="등록된 장소 검색..."
                    className="flex-1 px-4 py-3 border border-gray-300 rounded-lg text-base text-gray-900 placeholder:text-gray-400"
                  />
                  <button
                    type="button"
                    onClick={searchLocations}
                    className="px-4 py-3 bg-orange-500 text-white rounded-lg font-medium"
                  >
                    검색
                  </button>
                </div>
              </div>

              <div className="max-h-[40vh] overflow-y-auto">
                {existingLocations.length > 0 ? (
                  <div>
                    {existingLocations.map((location) => (
                      <button
                        key={location.id}
                        type="button"
                        onClick={() => selectLocation(location)}
                        className="w-full text-left px-4 py-4 border-b border-gray-100 active:bg-gray-50"
                      >
                        <p className="font-medium text-gray-900">{location.name}</p>
                        <p className="text-sm text-gray-500 mt-0.5">{location.address}</p>
                      </button>
                    ))}
                  </div>
                ) : (
                  <div className="text-center text-gray-500 py-12">
                    등록된 장소가 없습니다.
                  </div>
                )}
              </div>

              <div className="p-4 border-t border-gray-100 bg-gray-50">
                <button
                  type="button"
                  onClick={() => {
                    setShowLocationPicker(false);
                    router.push('/locations/new');
                  }}
                  className="w-full py-3.5 border-2 border-orange-500 text-orange-500 rounded-lg font-medium active:bg-orange-50"
                >
                  새 장소 추가하기
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Submit */}
        <div className="pt-4 pb-6">
          <button
            type="submit"
            disabled={loading || !title || !matchDate || !startTime || !endTime || !locationId || !validateDuration()}
            className="w-full py-3.5 bg-orange-500 text-white rounded-lg font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
          >
            {loading ? '생성 중...' : '경기 생성'}
          </button>
        </div>
      </form>
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
