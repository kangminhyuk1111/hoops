/**
 * Hoops 프로젝트 시스템 아키텍처 시각화
 *
 * Hexagonal Architecture (Ports & Adapters) + DDD
 * 도메인별 계층 구조 및 의존성 관계 표현
 */

import React from 'react';

const HoopsArchitectureDiagram = () => {
  const layers = {
    presentation: { color: '#E3F2FD', border: '#1976D2' },
    application: { color: '#F3E5F5', border: '#7B1FA2' },
    domain: { color: '#FFF3E0', border: '#F57C00' },
    infrastructure: { color: '#E8F5E9', border: '#388E3C' },
  };

  const domains = [
    {
      name: 'User',
      status: 'partial',
      color: '#FFE0B2',
      layers: ['Domain Model', 'UserId (VO)', 'Ports', 'UserEntity (minimal)'],
      completeness: 40,
    },
    {
      name: 'Auth',
      status: 'partial',
      color: '#FFE0B2',
      layers: ['AuthAccount', 'Provider', 'Ports', 'AuthAccountEntity'],
      completeness: 30,
    },
    {
      name: 'Match',
      status: 'complete',
      color: '#C8E6C9',
      layers: ['Match', 'MatchStatus', 'UseCases (5)', 'Services', 'MatchEntity', 'Controller'],
      completeness: 100,
    },
    {
      name: 'Participation',
      status: 'complete',
      color: '#C8E6C9',
      layers: ['Participation', 'ParticipationStatus', 'UseCases (2)', 'Services', 'ParticipationEntity', 'Controller'],
      completeness: 100,
    },
    {
      name: 'Location',
      status: 'partial',
      color: '#FFE0B2',
      layers: ['Location', 'Coordinate', 'Ports', 'LocationEntity'],
      completeness: 40,
    },
    {
      name: 'Notification',
      status: 'partial',
      color: '#FFE0B2',
      layers: ['Notification', 'NotificationType', 'Ports', 'NotificationEntity'],
      completeness: 30,
    },
  ];

  return (
    <div style={{ fontFamily: 'Arial, sans-serif', padding: '20px', backgroundColor: '#FAFAFA' }}>
      <h1 style={{ textAlign: 'center', color: '#263238', marginBottom: '10px' }}>
        Hoops 시스템 아키텍처
      </h1>
      <h3 style={{ textAlign: 'center', color: '#546E7A', marginBottom: '30px' }}>
        Hexagonal Architecture + DDD (Domain Driven Design)
      </h3>

      {/* 아키텍처 계층 설명 */}
      <div style={{ marginBottom: '30px', padding: '20px', backgroundColor: 'white', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
        <h2 style={{ color: '#263238', marginBottom: '15px' }}>📐 Hexagonal Architecture Layers</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '15px' }}>
          {[
            {
              name: 'Presentation',
              color: layers.presentation,
              description: 'REST Controllers, DTOs (record)',
              packages: 'adapter/in/web/'
            },
            {
              name: 'Application',
              color: layers.application,
              description: 'Use Cases, Ports (In/Out)',
              packages: 'application/port/, application/service/'
            },
            {
              name: 'Domain',
              color: layers.domain,
              description: 'Pure POJO, Business Logic',
              packages: 'domain/model/, domain/service/'
            },
            {
              name: 'Infrastructure',
              color: layers.infrastructure,
              description: 'JPA Entities, Repositories, Adapters',
              packages: 'infrastructure/'
            },
          ].map((layer) => (
            <div
              key={layer.name}
              style={{
                padding: '15px',
                backgroundColor: layer.color.color,
                border: `2px solid ${layer.color.border}`,
                borderRadius: '8px',
              }}
            >
              <h4 style={{ margin: '0 0 8px 0', color: layer.color.border }}>{layer.name}</h4>
              <p style={{ margin: '0 0 8px 0', fontSize: '14px', color: '#424242' }}>{layer.description}</p>
              <code style={{ fontSize: '11px', color: '#616161', backgroundColor: '#F5F5F5', padding: '4px 8px', borderRadius: '4px', display: 'block' }}>
                {layer.packages}
              </code>
            </div>
          ))}
        </div>
      </div>

      {/* 도메인별 구현 현황 */}
      <div style={{ marginBottom: '30px' }}>
        <h2 style={{ color: '#263238', marginBottom: '15px' }}>🎯 Domain Implementation Status</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px' }}>
          {domains.map((domain) => (
            <div
              key={domain.name}
              style={{
                padding: '20px',
                backgroundColor: domain.color,
                borderRadius: '12px',
                boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
                border: `3px solid ${domain.status === 'complete' ? '#4CAF50' : '#FF9800'}`,
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                <h3 style={{ margin: 0, color: '#263238' }}>
                  {domain.status === 'complete' ? '✅' : '⚠️'} {domain.name}
                </h3>
                <span
                  style={{
                    padding: '4px 12px',
                    backgroundColor: domain.status === 'complete' ? '#4CAF50' : '#FF9800',
                    color: 'white',
                    borderRadius: '12px',
                    fontSize: '12px',
                    fontWeight: 'bold',
                  }}
                >
                  {domain.completeness}%
                </span>
              </div>

              {/* Progress Bar */}
              <div style={{ width: '100%', height: '8px', backgroundColor: '#E0E0E0', borderRadius: '4px', marginBottom: '12px', overflow: 'hidden' }}>
                <div
                  style={{
                    width: `${domain.completeness}%`,
                    height: '100%',
                    backgroundColor: domain.status === 'complete' ? '#4CAF50' : '#FF9800',
                    transition: 'width 0.3s ease',
                  }}
                />
              </div>

              <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '13px', color: '#424242' }}>
                {domain.layers.map((layer, idx) => (
                  <li key={idx} style={{ marginBottom: '6px' }}>{layer}</li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </div>

      {/* 계층별 상세 구조 */}
      <div style={{ marginBottom: '30px', padding: '20px', backgroundColor: 'white', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
        <h2 style={{ color: '#263238', marginBottom: '15px' }}>🏗️ Layer Structure (Match Domain Example)</h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {[
            {
              layer: 'Presentation (adapter/in/web)',
              color: layers.presentation,
              items: ['MatchController', 'MatchResponse (record)', 'CreateMatchRequest (record)'],
            },
            {
              layer: 'Application (application/port & service)',
              color: layers.application,
              items: [
                'Ports In: FindMatchesUseCase, CreateMatchUseCase, StartMatchUseCase',
                'Ports Out: LoadMatchesPort, SaveMatchPort',
                'Services: MatchService (UseCase 구현)',
              ],
            },
            {
              layer: 'Domain (domain/model)',
              color: layers.domain,
              items: ['Match (Pure POJO)', 'MatchStatus (Enum)', 'Schedule (VO)', 'Business Logic Methods'],
            },
            {
              layer: 'Infrastructure (infrastructure/persistence)',
              color: layers.infrastructure,
              items: ['MatchEntity (@Entity)', 'MatchJpaRepository', 'MatchMapper (Entity ↔ Domain)', 'MatchPersistenceAdapter (Port 구현)'],
            },
          ].map((section) => (
            <div
              key={section.layer}
              style={{
                padding: '15px',
                backgroundColor: section.color.color,
                border: `2px solid ${section.color.border}`,
                borderRadius: '8px',
              }}
            >
              <h4 style={{ margin: '0 0 10px 0', color: section.color.border }}>{section.layer}</h4>
              <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '13px', color: '#424242' }}>
                {section.items.map((item, idx) => (
                  <li key={idx} style={{ marginBottom: '4px' }}>{item}</li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </div>

      {/* 도메인 간 의존성 관계 */}
      <div style={{ marginBottom: '30px', padding: '20px', backgroundColor: 'white', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
        <h2 style={{ color: '#263238', marginBottom: '15px' }}>🔗 Domain Dependencies</h2>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '15px' }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ padding: '15px 30px', backgroundColor: '#FFE0B2', borderRadius: '8px', border: '2px solid #FF9800', fontWeight: 'bold', fontSize: '16px' }}>
              User (사용자)
            </div>
            <div style={{ margin: '10px 0', fontSize: '24px', color: '#757575' }}>↓</div>
          </div>

          <div style={{ display: 'flex', gap: '20px', justifyContent: 'center', flexWrap: 'wrap' }}>
            {['Auth', 'Location'].map((domain) => (
              <div key={domain} style={{ padding: '12px 24px', backgroundColor: '#FFE0B2', borderRadius: '8px', border: '2px solid #FF9800', fontWeight: 'bold' }}>
                {domain}
              </div>
            ))}
          </div>

          <div style={{ margin: '10px 0', fontSize: '24px', color: '#757575' }}>↓</div>

          <div style={{ padding: '15px 30px', backgroundColor: '#C8E6C9', borderRadius: '8px', border: '2px solid #4CAF50', fontWeight: 'bold', fontSize: '16px' }}>
            Match (경기) ✅
          </div>

          <div style={{ margin: '10px 0', fontSize: '24px', color: '#757575' }}>↓</div>

          <div style={{ display: 'flex', gap: '20px', justifyContent: 'center', flexWrap: 'wrap' }}>
            <div style={{ padding: '12px 24px', backgroundColor: '#C8E6C9', borderRadius: '8px', border: '2px solid #4CAF50', fontWeight: 'bold' }}>
              Participation ✅
            </div>
            <div style={{ padding: '12px 24px', backgroundColor: '#FFE0B2', borderRadius: '8px', border: '2px solid #FF9800', fontWeight: 'bold' }}>
              Notification
            </div>
          </div>
        </div>

        <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#F5F5F5', borderRadius: '8px', fontSize: '13px', color: '#616161' }}>
          <strong>의존 방향:</strong>
          <ul style={{ margin: '8px 0 0 0', paddingLeft: '20px' }}>
            <li>Match → User (hostId FK)</li>
            <li>Participation → Match, User (FK)</li>
            <li>Notification → User, Match (FK)</li>
            <li>AuthAccount → User (FK)</li>
            <li>Location → User (FK)</li>
          </ul>
        </div>
      </div>

      {/* 아키텍처 원칙 */}
      <div style={{ padding: '20px', backgroundColor: 'white', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
        <h2 style={{ color: '#263238', marginBottom: '15px' }}>📋 Architecture Principles</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '15px' }}>
          {[
            {
              icon: '✅',
              title: 'Domain 격리',
              description: 'Domain Layer는 외부 라이브러리 의존성 없는 Pure POJO',
            },
            {
              icon: '✅',
              title: 'Entity 분리',
              description: 'Domain Model ↔ JPA Entity 분리, Mapper를 통한 변환',
            },
            {
              icon: '✅',
              title: 'No Lombok',
              description: '명시적 생성자/Getter/Setter 작성',
            },
            {
              icon: '✅',
              title: 'Constructor Injection',
              description: '모든 의존성은 생성자 주입 (@Autowired 필드 주입 금지)',
            },
            {
              icon: '✅',
              title: 'DTO vs Entity',
              description: 'Controller는 record DTO 반환 (Entity 직접 반환 금지)',
            },
            {
              icon: '✅',
              title: 'Exception Hierarchy',
              description: 'BusinessException 계층 구조 (Domain/Application/Infrastructure)',
            },
          ].map((principle) => (
            <div
              key={principle.title}
              style={{
                padding: '15px',
                backgroundColor: '#F1F8E9',
                border: '2px solid #8BC34A',
                borderRadius: '8px',
              }}
            >
              <div style={{ fontSize: '24px', marginBottom: '8px' }}>{principle.icon}</div>
              <h4 style={{ margin: '0 0 8px 0', color: '#33691E' }}>{principle.title}</h4>
              <p style={{ margin: 0, fontSize: '13px', color: '#558B2F' }}>{principle.description}</p>
            </div>
          ))}
        </div>
      </div>

      {/* 범례 */}
      <div style={{ marginTop: '30px', padding: '15px', backgroundColor: '#ECEFF1', borderRadius: '8px', textAlign: 'center' }}>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '30px', flexWrap: 'wrap', fontSize: '14px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div style={{ width: '20px', height: '20px', backgroundColor: '#C8E6C9', border: '2px solid #4CAF50', borderRadius: '4px' }}></div>
            <span><strong>✅ 완전 구현</strong> (100%)</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div style={{ width: '20px', height: '20px', backgroundColor: '#FFE0B2', border: '2px solid #FF9800', borderRadius: '4px' }}></div>
            <span><strong>⚠️ 부분 구현</strong> (30-40%)</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HoopsArchitectureDiagram;
