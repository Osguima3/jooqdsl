CREATE TYPE custom_enum AS ENUM (
    'ENABLED',
    'DISABLED'
);

CREATE TABLE test
(
    int            INT,
    string         TEXT,
    big_decimal    NUMERIC(10, 4),
    value_object   TEXT,
    instant_object TIMESTAMP WITH TIME ZONE,
    json           JSONB,
    custom_enum    CUSTOM_ENUM,
    string_enum    TEXT,
    composite      TEXT,
    converter      TEXT,
    custom         TEXT
);
