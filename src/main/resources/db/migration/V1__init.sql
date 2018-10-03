CREATE EXTENSION IF NOT EXISTS CITEXT;

CREATE TABLE "user" (
  id SERIAL PRIMARY KEY,
  login CITEXT UNIQUE NOT NULL,
  password CITEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS page (
  uuid TEXT UNIQUE,
  ownerID INTEGER,
  title TEXT,
  isPublic BOOLEAN,
  fieldsNames TEXT[],
  fieldsValues TEXT[]
);

CREATE TABLE IF NOT EXISTS userPages (
  userID INTEGER,
  pageUUID TEXT,
  title TEXT,
  UNIQUE (userID, pageUUID)
);

