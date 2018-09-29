create table if not exists userpublic (
  id serial primary key,
  login text unique not null,
  password text not null
);