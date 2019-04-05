CREATE TYPE custom_enum AS ENUM (
  'ENABLED',
  'DISABLED'
  );

CREATE TABLE test
(
  uuid        UUID PRIMARY KEY,
  string      TEXT,
  instant     TIMESTAMP WITH TIME ZONE,
  int         INT,
  big_decimal NUMERIC(10, 4),
  custom_enum CUSTOM_ENUM,
  string_enum TEXT,
  custom      TEXT
);
