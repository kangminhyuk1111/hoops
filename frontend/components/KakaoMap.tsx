'use client';

import { useEffect, useRef, useState } from 'react';
import { Match } from '@/types';

declare global {
  interface Window {
    kakao: any;
  }
}

interface KakaoMapProps {
  matches: Match[];
  center: { latitude: number; longitude: number };
  currentLocation?: { latitude: number; longitude: number };
  onMarkerClick?: (match: Match) => void;
}

export default function KakaoMap({ matches, center, currentLocation, onMarkerClick }: KakaoMapProps) {
  const mapRef = useRef<HTMLDivElement>(null);
  const [map, setMap] = useState<any>(null);
  const [isLoaded, setIsLoaded] = useState(false);
  const markersRef = useRef<any[]>([]);
  const currentLocationMarkerRef = useRef<any>(null);

  // 카카오 맵 SDK 로드
  useEffect(() => {
    const kakaoKey = process.env.NEXT_PUBLIC_KAKAO_JS_KEY;
    console.log('[KakaoMap] JS Key:', kakaoKey ? 'exists' : 'missing');

    const script = document.getElementById('kakao-map-script');

    if (!script) {
      const scriptUrl = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoKey}&autoload=false&libraries=services`;
      console.log('[KakaoMap] Creating new script, URL:', scriptUrl);
      const newScript = document.createElement('script');
      newScript.id = 'kakao-map-script';
      newScript.src = scriptUrl;
      newScript.async = true;
      newScript.onload = () => {
        console.log('[KakaoMap] Script loaded, calling kakao.maps.load()');
        window.kakao.maps.load(() => {
          console.log('[KakaoMap] Maps SDK ready');
          setIsLoaded(true);
        });
      };
      newScript.onerror = (e) => {
        console.error('[KakaoMap] Script load error:', e);
      };
      document.head.appendChild(newScript);
    } else if (window.kakao && window.kakao.maps) {
      console.log('[KakaoMap] Script already exists, SDK ready');
      setIsLoaded(true);
    } else {
      console.log('[KakaoMap] Script exists but SDK not ready');
    }
  }, []);

  // 지도 초기화
  useEffect(() => {
    if (!isLoaded || !mapRef.current) return;

    const options = {
      center: new window.kakao.maps.LatLng(center.latitude, center.longitude),
      level: 5,
    };

    const newMap = new window.kakao.maps.Map(mapRef.current, options);
    setMap(newMap);
  }, [isLoaded, center.latitude, center.longitude]);

  // 마커 생성
  useEffect(() => {
    if (!map || !isLoaded) return;

    console.log('[KakaoMap] Creating markers for matches:', matches.map(m => ({
      id: m.id,
      title: m.title,
      lat: m.latitude,
      lng: m.longitude
    })));

    // 기존 마커 제거
    markersRef.current.forEach((marker) => marker.setMap(null));
    markersRef.current = [];

    // 새 마커 생성
    matches.forEach((match) => {
      const position = new window.kakao.maps.LatLng(match.latitude, match.longitude);

      const marker = new window.kakao.maps.Marker({
        position,
        map,
      });

      // 인포윈도우 내용
      const content = `
        <div style="padding:8px 12px;font-size:13px;min-width:150px;">
          <div style="font-weight:600;margin-bottom:4px;">${match.title}</div>
          <div style="color:#666;font-size:12px;">${match.currentParticipants}/${match.maxParticipants}명</div>
        </div>
      `;

      const infowindow = new window.kakao.maps.InfoWindow({
        content,
      });

      // 마커 클릭 이벤트
      window.kakao.maps.event.addListener(marker, 'click', () => {
        if (onMarkerClick) {
          onMarkerClick(match);
        }
      });

      // 마커 호버 이벤트
      window.kakao.maps.event.addListener(marker, 'mouseover', () => {
        infowindow.open(map, marker);
      });

      window.kakao.maps.event.addListener(marker, 'mouseout', () => {
        infowindow.close();
      });

      markersRef.current.push(marker);
    });
  }, [map, matches, isLoaded, onMarkerClick]);

  // 현재 위치 마커 (빨간색)
  useEffect(() => {
    if (!map || !isLoaded || !currentLocation) return;

    // 기존 현재 위치 마커 제거
    if (currentLocationMarkerRef.current) {
      currentLocationMarkerRef.current.setMap(null);
    }

    const position = new window.kakao.maps.LatLng(
      currentLocation.latitude,
      currentLocation.longitude
    );

    // 빨간색 커스텀 마커 이미지 (SVG data URL)
    const markerSvg = `
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
        <circle cx="12" cy="12" r="8" fill="#EF4444" stroke="#ffffff" stroke-width="3"/>
      </svg>
    `;
    const markerImageSrc = 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(markerSvg);

    const markerImage = new window.kakao.maps.MarkerImage(
      markerImageSrc,
      new window.kakao.maps.Size(24, 24),
      { offset: new window.kakao.maps.Point(12, 12) }
    );

    const marker = new window.kakao.maps.Marker({
      position,
      map,
      image: markerImage,
      zIndex: 10,
    });

    // 현재 위치 인포윈도우
    const content = `
      <div style="padding:6px 10px;font-size:12px;white-space:nowrap;">
        <span style="color:#EF4444;font-weight:600;">내 위치</span>
      </div>
    `;
    const infowindow = new window.kakao.maps.InfoWindow({ content });

    window.kakao.maps.event.addListener(marker, 'mouseover', () => {
      infowindow.open(map, marker);
    });
    window.kakao.maps.event.addListener(marker, 'mouseout', () => {
      infowindow.close();
    });

    currentLocationMarkerRef.current = marker;
  }, [map, isLoaded, currentLocation]);

  if (!isLoaded) {
    return (
      <div className="absolute inset-0 bg-gray-100 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-2 border-orange-500 border-t-transparent"></div>
      </div>
    );
  }

  return <div ref={mapRef} className="absolute inset-0" />;
}
