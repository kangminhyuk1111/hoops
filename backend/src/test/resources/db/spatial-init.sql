ALTER TABLE matches ADD COLUMN location POINT AS (ST_SRID(POINT(longitude, latitude), 4326)) STORED NOT NULL;
CREATE SPATIAL INDEX idx_match_location ON matches(location);

ALTER TABLE locations ADD COLUMN location POINT AS (ST_SRID(POINT(longitude, latitude), 4326)) STORED NOT NULL;
CREATE SPATIAL INDEX idx_location_location ON locations(location);
