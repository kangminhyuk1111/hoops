// User
export interface User {
  id: number;
  nickname: string;
  profileImageUrl?: string;
  createdAt: string;
}

// Match
export interface Match {
  id: number;
  title: string;
  description?: string;
  startTime: string;
  endTime: string;
  maxParticipants: number;
  currentParticipants: number;
  status: MatchStatus;
  host: HostInfo;
  location: LocationInfo;
  createdAt: string;
}

export type MatchStatus = 'PENDING' | 'IN_PROGRESS' | 'ENDED' | 'CANCELLED';

export interface HostInfo {
  id: number;
  nickname: string;
  profileImageUrl?: string;
}

export interface LocationInfo {
  id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
}

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

// API Response
export interface ApiError {
  errorCode: string;
  message: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
