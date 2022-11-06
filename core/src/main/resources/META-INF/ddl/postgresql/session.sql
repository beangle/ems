CREATE TABLE ems.se_session_infoes
(
    id character varying(200) NOT NULL,
    principal character varying(200),
    data bytea,
    description character varying(200),
    ip character varying(200),
    agent character varying(200),
    category_id int4,
    domain_id int4,
    os character varying(200),
    login_at timestamp without time zone,
    tti_seconds int4,
    last_access_at timestamp without time zone,
    CONSTRAINT session_infoes_pkey PRIMARY KEY (id)
);
