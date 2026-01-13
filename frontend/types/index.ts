// User
export interface User {
  id: number;
  nickname: string;
  profileImageUrl?: string;
  createdAt: string;
}

// Match (API 응답 형식)
export interface Match {
  id: number;
  hostId: number;
  hostNickname: string;
  title: string;
  description?: string;
  latitude: number;
  longitude: number;
  address: string;
  matchDate: string;
  startTime: string;
  endTime: string;
  maxParticipants: number;
  currentParticipants: number;
  status: MatchStatus;
}

export type MatchStatus = 'PENDING' | 'IN_PROGRESS' | 'ENDED' | 'CANCELLED';

// Participation
export interface Participation {
  id: number;
  matchId: number;
  userId: number;
  status: ParticipationStatus;
  createdAt: string;
}

export type ParticipationStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'MATCH_CANCELLED';

export interface Participant {
  id: number;
  participationId: number;
  nickname: string;
  profileImageUrl?: string;
  status: ParticipationStatus;
}

// Notification
export interface Notification {
  id: number;
  type: NotificationType;
  message: string;
  referenceId?: number;
  isRead: boolean;
  createdAt: string;
}

export type NotificationType =
  | 'PARTICIPATION_REQUEST'
  | 'PARTICIPATION_APPROVED'
  | 'PARTICIPATION_REJECTED'
  | 'MATCH_CANCELLED';

// Location
export interface Location {
  id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  description?: string;
}

export interface CreateLocationRequest {
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  description?: string;
}

export interface CreateMatchRequest {
  locationId: number;
  title: string;
  description?: string;
  matchDate: string;
  startTime: string;
  endTime: string;
  maxParticipants: number;
}

// Participant Detail (API response)
export interface ParticipantDetail {
  id: number;
  matchId: number;
  userId: number;
  nickname: string;
  profileImage?: string;
  rating: number;
  totalMatches: number;
  status: ParticipationStatus;
  joinedAt: string;
}

// API Response
export interface ApiError {
  errorCode: string;
  message: string;
}
