CREATE EXTENSION IF NOT EXISTS CITEXT;

CREATE TABLE "user" (
  id SERIAL PRIMARY KEY,
  login TEXT,
  email TEXT UNIQUE,
  token CITEXT

);

CREATE TABLE IF NOT EXISTS page (
  uuid TEXT UNIQUE,
  ownerID INTEGER,
  title TEXT,
  isPublic BOOLEAN,
  fieldsNames TEXT[],
  fieldsValues TEXT[],
  date TEXT
);

CREATE TABLE IF NOT EXISTS userPages (
  userID INTEGER,
  pageUUID TEXT,
  title TEXT,
  date TEXT,
  UNIQUE (userID, pageUUID)
);

