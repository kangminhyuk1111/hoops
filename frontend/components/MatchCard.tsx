import { Match, MatchStatus, RecruitmentStatus } from '@/types';

interface MatchCardProps {
  match: Match;
  onClick?: () => void;
}

const STATUS_LABEL: Record<MatchStatus, string> = {
  PENDING: '모집중',
  IN_PROGRESS: '진행중',
  ENDED: '종료',
  CANCELLED: '취소됨',
};

const STATUS_COLOR: Record<MatchStatus, string> = {
  PENDING: 'bg-green-100 text-green-700',
  IN_PROGRESS: 'bg-blue-100 text-blue-700',
  ENDED: 'bg-gray-100 text-gray-600',
  CANCELLED: 'bg-red-100 text-red-600',
};

const RECRUITMENT_LABEL: Record<RecruitmentStatus, string> = {
  RECRUITING: '모집중',
  ALMOST_FULL: '마감임박',
  FULL: '마감',
};

const RECRUITMENT_COLOR: Record<RecruitmentStatus, string> = {
  RECRUITING: 'text-green-600',
  ALMOST_FULL: 'text-orange-500',
  FULL: 'text-red-500',
};

export default function MatchCard({ match, onClick }: MatchCardProps) {
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
    const weekday = weekdays[date.getDay()];
    return `${month}/${day} (${weekday})`;
  };

  const formatTime = (timeStr: string) => {
    return timeStr.slice(0, 5);
  };

  return (
    <div
      onClick={onClick}
      className="bg-white rounded-xl p-4 shadow-sm active:bg-gray-50 cursor-pointer"
    >
      <div className="flex justify-between items-start mb-2">
        <h3 className="font-semibold text-gray-800 text-base line-clamp-1">
          {match.title}
        </h3>
        <span
          className={`text-xs px-2 py-0.5 rounded-full ${STATUS_COLOR[match.status]}`}
        >
          {STATUS_LABEL[match.status]}
        </span>
      </div>

      <div className="flex items-center gap-2 mb-3">
        <p className="text-sm text-gray-500 line-clamp-1 flex-1">{match.address}</p>
        {match.distanceKm != null && (
          <span className="text-xs text-orange-500 font-medium whitespace-nowrap">
            {match.distanceKm < 1
              ? `${Math.round(match.distanceKm * 1000)}m`
              : `${match.distanceKm.toFixed(1)}km`}
          </span>
        )}
      </div>

      <div className="flex items-center justify-between text-sm">
        <div className="flex items-center gap-3 text-gray-600">
          <span>{formatDate(match.matchDate)}</span>
          <span>
            {formatTime(match.startTime)} - {formatTime(match.endTime)}
          </span>
        </div>
        <div className="flex items-center gap-2 text-gray-600">
          <span className={`text-xs font-medium ${RECRUITMENT_COLOR[match.recruitmentStatus]}`}>
            {RECRUITMENT_LABEL[match.recruitmentStatus]}
          </span>
          <span className="text-sm">
            <span className="text-orange-500 font-medium">
              {match.currentParticipants}
            </span>
            /{match.maxParticipants}명
            {match.remainingSlots > 0 && (
              <span className="text-gray-400 ml-1">({match.remainingSlots}자리)</span>
            )}
          </span>
        </div>
      </div>

      <div className="mt-3 pt-3 border-t border-gray-100">
        <span className="text-xs text-gray-400">
          호스트: {match.hostNickname}
        </span>
      </div>
    </div>
  );
}
