'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/store/auth';
import api from '@/lib/api';
import { CreateLocationRequest } from '@/types';

declare global {
  interface Window {
    kakao: any;
  }
}

interface PlaceResult {
  id: string;
  place_name: string;
  address_name: string;
  road_address_name?: string;
  x: string;
  y: string;
  category_name?: string;
}

// 기본 위치: 서울 시청
const DEFAULT_LOCATION = {
  latitude: 37.5665,
  longitude: 126.978,
};

export default function CreateLocationPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuthStore();
  const mapRef = useRef<HTMLDivElement>(null);

  const [isMapLoaded, setIsMapLoaded] = useState(false);
  const [map, setMap] = useState<any>(null);
  const [marker, setMarker] = useState<any>(null);

  // Form state
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [latitude, setLatitude] = useState<number>(DEFAULT_LOCATION.latitude);
  const [longitude, setLongitude] = useState<number>(DEFAULT_LOCATION.longitude);
  const [description, setDescription] = useState('');

  // Search state
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState<PlaceResult[]>([]);
  const [showResults, setShowResults] = useState(false);

  // UI state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, router]);

  // 카카오 맵 SDK 로드
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
      if (window.kakao.maps.services) {
        setIsMapLoaded(true);
      } else {
        window.kakao.maps.load(() => {
          setIsMapLoaded(true);
        });
      }
    }
  }, []);

  // 지도 초기화
  useEffect(() => {
    if (!isMapLoaded || !mapRef.current) return;

    const options = {
      center: new window.kakao.maps.LatLng(latitude, longitude),
      level: 3,
    };

    const newMap = new window.kakao.maps.Map(mapRef.current, options);
    setMap(newMap);

    const newMarker = new window.kakao.maps.Marker({
      position: new window.kakao.maps.LatLng(latitude, longitude),
      map: newMap,
    });
    setMarker(newMarker);

    window.kakao.maps.event.addListener(newMap, 'click', (mouseEvent: any) => {
      const latlng = mouseEvent.latLng;
      const lat = latlng.getLat();
      const lng = latlng.getLng();

      setLatitude(lat);
      setLongitude(lng);
      newMarker.setPosition(latlng);
      searchAddressByCoord(lat, lng);
    });
  }, [isMapLoaded]);

  // 좌표로 주소 검색
  const searchAddressByCoord = (lat: number, lng: number) => {
    if (!window.kakao?.maps?.services) return;

    const geocoder = new window.kakao.maps.services.Geocoder();

    geocoder.coord2Address(lng, lat, (result: any, status: any) => {
      if (status === window.kakao.maps.services.Status.OK) {
        const addr = result[0].road_address
          ? result[0].road_address.address_name
          : result[0].address.address_name;
        setAddress(addr);
      }
    });
  };

  // 카카오 Places API로 장소 검색
  const searchPlaces = () => {
    if (!searchKeyword.trim() || !window.kakao?.maps?.services) return;

    const ps = new window.kakao.maps.services.Places();

    ps.keywordSearch(searchKeyword, (data: PlaceResult[], status: any) => {
      if (status === window.kakao.maps.services.Status.OK) {
        setSearchResults(data.slice(0, 10));
        setShowResults(true);
      } else if (status === window.kakao.maps.services.Status.ZERO_RESULT) {
        setSearchResults([]);
        setShowResults(true);
      }
    });
  };

  // 검색 결과 선택
  const selectPlace = (place: PlaceResult) => {
    const lat = parseFloat(place.y);
    const lng = parseFloat(place.x);

    setName(place.place_name);
    setAddress(place.road_address_name || place.address_name);
    setLatitude(lat);
    setLongitude(lng);
    setShowResults(false);
    setSearchKeyword('');

    // 지도 이동
    const moveLatLng = new window.kakao.maps.LatLng(lat, lng);
    map?.setCenter(moveLatLng);
    marker?.setPosition(moveLatLng);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!name || !address || !latitude || !longitude) {
      setError('모든 필수 정보를 입력해주세요.');
      return;
    }

    setLoading(true);

    try {
      const request: CreateLocationRequest = {
        name,
        address,
        latitude,
        longitude,
        description: description || undefined,
      };

      await api.post('/api/locations', request);
      router.back();
    } catch (err: any) {
      console.error('장소 생성 실패:', err);
      setError(err.response?.data?.message || '장소 생성에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-30">
        <div className="px-4 py-3 flex items-center gap-3">
          <button onClick={() => router.back()} className="text-gray-600">
            <BackIcon />
          </button>
          <h1 className="text-lg font-semibold text-gray-800">장소 추가</h1>
        </div>
      </header>

      {/* 장소 검색 */}
      <div className="px-4 py-3 bg-white border-b relative z-20">
        <div className="flex gap-2">
          <input
            type="text"
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && searchPlaces()}
            placeholder="장소명으로 검색 (예: 농구장, 체육관)"
            className="flex-1 px-4 py-3 border border-gray-300 rounded-lg text-base text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-orange-500"
          />
          <button
            type="button"
            onClick={searchPlaces}
            className="px-4 py-3 bg-orange-500 text-white rounded-lg font-medium"
          >
            검색
          </button>
        </div>

        {/* 검색 결과 */}
        {showResults && (
          <div className="absolute left-0 right-0 top-full bg-white border-b shadow-lg max-h-[300px] overflow-y-auto">
            {searchResults.length > 0 ? (
              searchResults.map((place) => (
                <button
                  key={place.id}
                  type="button"
                  onClick={() => selectPlace(place)}
                  className="w-full text-left px-4 py-3 border-b border-gray-100 active:bg-gray-50"
                >
                  <p className="font-medium text-gray-900">{place.place_name}</p>
                  <p className="text-sm text-gray-500 mt-0.5">
                    {place.road_address_name || place.address_name}
                  </p>
                  {place.category_name && (
                    <p className="text-xs text-gray-400 mt-0.5">{place.category_name}</p>
                  )}
                </button>
              ))
            ) : (
              <div className="px-4 py-6 text-center text-gray-500">
                검색 결과가 없습니다.
              </div>
            )}
            <button
              type="button"
              onClick={() => setShowResults(false)}
              className="w-full py-3 text-gray-500 text-sm bg-gray-50"
            >
              닫기
            </button>
          </div>
        )}
      </div>

      {/* Map */}
      <div className="h-[200px] relative bg-gray-100">
        {isMapLoaded ? (
          <div ref={mapRef} className="absolute inset-0" />
        ) : (
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="animate-spin rounded-full h-8 w-8 border-2 border-orange-500 border-t-transparent"></div>
          </div>
        )}
        <div className="absolute bottom-3 left-3 bg-white rounded-lg px-3 py-2 shadow text-xs text-gray-600">
          지도를 클릭하여 위치 조정
        </div>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="flex-1 p-4 space-y-4">
        {error && (
          <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm">
            {error}
          </div>
        )}

        {/* 장소 이름 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">
            장소 이름 *
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="예: OO 초등학교 농구장"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg text-base text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-orange-500"
            required
          />
        </div>

        {/* 주소 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">
            주소 *
          </label>
          <input
            type="text"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            placeholder="검색 또는 지도 클릭으로 자동 입력"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg text-base text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-orange-500"
            required
          />
        </div>

        {/* 좌표 (읽기 전용) */}
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              위도
            </label>
            <input
              type="text"
              value={latitude.toFixed(6)}
              readOnly
              className="w-full px-4 py-3 bg-gray-100 border border-gray-200 rounded-lg text-sm text-gray-600"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              경도
            </label>
            <input
              type="text"
              value={longitude.toFixed(6)}
              readOnly
              className="w-full px-4 py-3 bg-gray-100 border border-gray-200 rounded-lg text-sm text-gray-600"
            />
          </div>
        </div>

        {/* 설명 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">
            설명 (선택)
          </label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="장소에 대한 추가 정보"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg text-base text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-orange-500 h-20 resize-none"
          />
        </div>

        {/* Submit */}
        <div className="pt-2">
          <button
            type="submit"
            disabled={loading || !name || !address}
            className="w-full py-3.5 bg-orange-500 text-white rounded-lg font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
          >
            {loading ? '등록 중...' : '장소 등록'}
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
