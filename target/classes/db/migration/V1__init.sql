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
  isStatic BOOLEAN,
  template TEXT,
  fieldsNames TEXT[],
  fieldsValues TEXT[],
  innerPages TEXT[],
  date TEXT,
  standalone BOOLEAN
);

CREATE TABLE IF NOT EXISTS userPages (
  userID INTEGER,
  pageUUID TEXT,
  UNIQUE (userID, pageUUID)
);

