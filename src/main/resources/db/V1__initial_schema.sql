SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET search_path = public, pg_catalog;

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;

SET search_path = public, pg_catalog;

CREATE TYPE at_authentication_type AS ENUM (
    'email',
    'google'
);

CREATE TYPE at_direction AS ENUM (
    'nobo',
    'sobo'
);
CREATE TYPE at_poi_type AS ENUM (
    'shelter',
    'parking',
    'terminus',
    'other'
);

CREATE TYPE at_role AS ENUM (
    'unconfirmed',
    'user',
    'user.payed'
);

SET default_tablespace = '';
SET default_with_oids = false;

CREATE TABLE at_confirmation (
    id integer NOT NULL,
    at_user integer NOT NULL,
    token uuid NOT NULL,
    send_date date DEFAULT now() NOT NULL
);

CREATE SEQUENCE at_confirmation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE at_confirmation_id_seq OWNED BY at_confirmation.id;
ALTER TABLE ONLY at_confirmation ALTER COLUMN id SET DEFAULT nextval('at_confirmation_id_seq'::regclass);

CREATE TABLE at_poi (
    id integer NOT NULL,
    name text NOT NULL,
    comment text,
    point point NOT NULL,
    campground boolean,
    elevation real,
    type at_poi_type DEFAULT 'other'::at_poi_type NOT NULL
);

CREATE SEQUENCE at_shelter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE at_shelter_id_seq OWNED BY at_poi.id;
ALTER TABLE ONLY at_poi ALTER COLUMN id SET DEFAULT nextval('at_shelter_id_seq'::regclass);

CREATE TABLE at_preferences (
    id integer NOT NULL,
    "user" integer NOT NULL,
    direction at_direction DEFAULT 'nobo'::at_direction NOT NULL,
    start_end daterange DEFAULT 'empty'::daterange NOT NULL,
    show_whole_trail boolean DEFAULT false NOT NULL,
    realName text,
    trailName text
);

CREATE SEQUENCE at_preferencs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE at_preferencs_id_seq OWNED BY at_preferences.id;
ALTER TABLE ONLY at_preferences ALTER COLUMN id SET DEFAULT nextval('at_preferencs_id_seq'::regclass);

CREATE TABLE at_quota (
    id integer NOT NULL,
    at_user integer NOT NULL,
    month smallint DEFAULT date_part('month'::text, ('now'::text)::date) NOT NULL,
    quota integer DEFAULT 1000 NOT NULL
);

CREATE SEQUENCE at_quota_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE at_quota_id_seq OWNED BY at_quota.id;
ALTER TABLE ONLY at_quota ALTER COLUMN id SET DEFAULT nextval('at_quota_id_seq'::regclass);

CREATE TABLE at_session (
    id integer NOT NULL,
    at_user integer NOT NULL,
    token uuid NOT NULL
);

CREATE SEQUENCE at_session_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE at_session_id_seq OWNED BY at_session.id;
ALTER TABLE ONLY at_session ALTER COLUMN id SET DEFAULT nextval('at_session_id_seq'::regclass);

CREATE TABLE at_timeline (
    id bigint NOT NULL,
    at_user integer NOT NULL,
    at_poi integer NOT NULL,
    comment text,
    date date NOT NULL
);

CREATE SEQUENCE at_timeline_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE at_timeline_id_seq OWNED BY at_timeline.id;
ALTER TABLE ONLY at_timeline ALTER COLUMN id SET DEFAULT nextval('at_timeline_id_seq'::regclass);

CREATE TABLE at_trail (
    id integer NOT NULL,
    name text NOT NULL,
    abbrv text,
    path path,
    points point[]
);

CREATE SEQUENCE at_trail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE at_trail_id_seq OWNED BY at_trail.id;
ALTER TABLE ONLY at_trail ALTER COLUMN id SET DEFAULT nextval('at_trail_id_seq'::regclass);

CREATE TABLE at_user (
    id integer NOT NULL,
    email text NOT NULL,
    password text NOT NULL,
    role at_role DEFAULT 'unconfirmed'::at_role NOT NULL,
    registration_date date DEFAULT now() NOT NULL,
    authentication_type at_authentication_type DEFAULT 'email'::at_authentication_type NOT NULL
);

CREATE SEQUENCE at_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE at_user_id_seq OWNED BY at_user.id;
ALTER TABLE ONLY at_user ALTER COLUMN id SET DEFAULT nextval('at_user_id_seq'::regclass);


CREATE VIEW at_last_poi WITH (security_barrier='false') AS
    SELECT DISTINCT ON (tt.at_user) tt.at_user,
        tt.at_poi,
        tt.date,
        pp.name,
        pp.point
    FROM ((at_timeline tt
        JOIN ( SELECT at_timeline.at_user,
                   max(at_timeline.date) AS last_date
               FROM at_timeline
               GROUP BY at_timeline.at_user) ll ON (((tt.date = ll.last_date) AND (tt.at_user = ll.at_user))))
        JOIN at_poi pp ON ((tt.at_poi = pp.id)));


ALTER TABLE ONLY at_trail
    ADD CONSTRAINT "AT_trail_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY at_confirmation
    ADD CONSTRAINT at_confirmation_pkey PRIMARY KEY (id);

ALTER TABLE ONLY at_preferences
    ADD CONSTRAINT at_preferencs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY at_preferences
    ADD CONSTRAINT at_preferencs_user_key UNIQUE ("user");

ALTER TABLE ONLY at_quota
    ADD CONSTRAINT at_quota_pkey PRIMARY KEY (id);

ALTER TABLE at_quota
    ADD CONSTRAINT at_quota_quota_check CHECK ((quota >= 0)) NOT VALID;

ALTER TABLE ONLY at_session
    ADD CONSTRAINT at_session_at_user_key UNIQUE (at_user);

ALTER TABLE ONLY at_session
    ADD CONSTRAINT at_session_pkey PRIMARY KEY (id);

ALTER TABLE ONLY at_session
    ADD CONSTRAINT at_session_token_key UNIQUE (token);

ALTER TABLE ONLY at_poi
    ADD CONSTRAINT at_shelter_pkey PRIMARY KEY (id);

ALTER TABLE ONLY at_timeline
    ADD CONSTRAINT at_timeline_pkey PRIMARY KEY (id);

ALTER TABLE ONLY at_user
    ADD CONSTRAINT at_user_email_key UNIQUE (email);

ALTER TABLE ONLY at_user
    ADD CONSTRAINT at_user_pkey PRIMARY KEY (id);


CREATE UNIQUE INDEX at_user_lower_idx ON at_user USING btree (lower(email));


ALTER TABLE ONLY at_confirmation
    ADD CONSTRAINT at_confirmation_at_user_fkey FOREIGN KEY (at_user) REFERENCES at_user(id);

ALTER TABLE ONLY at_preferences
    ADD CONSTRAINT at_preferencs_user_fkey FOREIGN KEY ("user") REFERENCES at_user(id);

ALTER TABLE ONLY at_quota
    ADD CONSTRAINT at_quota_at_user_fkey FOREIGN KEY (at_user) REFERENCES at_user(id);

ALTER TABLE ONLY at_session
    ADD CONSTRAINT at_session_user_fkey FOREIGN KEY (at_user) REFERENCES at_user(id);

ALTER TABLE ONLY at_timeline
    ADD CONSTRAINT at_timeline_at_poi_fkey FOREIGN KEY (at_poi) REFERENCES at_poi(id);

ALTER TABLE ONLY at_timeline
    ADD CONSTRAINT at_timeline_at_user_fkey FOREIGN KEY (at_user) REFERENCES at_user(id);
