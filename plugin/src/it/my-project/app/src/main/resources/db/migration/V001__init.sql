CREATE TYPE legal_entity_type AS ENUM (
  'ANONYMOUS',
  'LIMITED',
  'PUBLIC'
  );

CREATE TABLE company
(
  id            INT PRIMARY KEY,
  name          TEXT,
  creation_date TIMESTAMP WITH TIME ZONE,
  employees     INT,
  valuation     NUMERIC(10, 4),
  legal_type    legal_entity_type,
  industry      TEXT
);
