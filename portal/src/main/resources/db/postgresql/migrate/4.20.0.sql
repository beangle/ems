alter table ems.usr_roles drop column if exists env_ids;

alter table ems.usr_profiles rename to usr_env_profiles;
alter table ems.usr_env_profiles add properties jsonb default '{}';

UPDATE ems.usr_env_profiles AS p
SET properties = COALESCE(j.props, '{}'::jsonb)
  FROM (
  SELECT
    t.profile_id,
    jsonb_object_agg(t.dim_name, t.dim_value) AS props
  FROM (
    SELECT
      pp.profile_id,
      d.name AS dim_name,
      string_agg(pp.value_, ',' ORDER BY pp.value_) AS dim_value
    FROM ems.usr_profiles_properties pp
    JOIN ems.usr_dimensions d ON d.id = pp.dimension_id
    GROUP BY pp.profile_id, d.name
  ) t
  GROUP BY t.profile_id
) j
WHERE p.id = j.profile_id;

alter table ems.usr_env_profiles add env_id bigint;

UPDATE ems.usr_env_profiles
SET env_id = (properties->>'project')::bigint
WHERE properties ? 'project'
  AND (properties->>'project') ~ '^[0-9]+$';

CREATE TABLE IF NOT EXISTS ems.cfg_envs (
                                          id         bigint       NOT NULL,
                                          name       varchar(100) NOT NULL,
  domain_id  integer      NOT NULL,
  CONSTRAINT pk_cfg_envs PRIMARY KEY (id),
  CONSTRAINT fk_cfg_envs_domain FOREIGN KEY (domain_id)
  REFERENCES ems.cfg_domains (id),
  CONSTRAINT idx_env UNIQUE (domain_id, name)
  );

WITH dim AS (
  SELECT domain_id, source_
  FROM ems.usr_dimensions
  WHERE name = 'project' and domain_id=1
),
     lines AS (
       SELECT
         d.domain_id,
         trim(t.line) AS line,
         t.ord
       FROM dim d
              CROSS JOIN LATERAL regexp_split_to_table(
                                                       regexp_replace(d.source_, '^csv:\s*', '', 'i'),
                                                       E'\n'
         ) WITH ORDINALITY AS t(line, ord)
WHERE trim(t.line) <> ''
  ),
  parsed AS (
SELECT
  domain_id,
  string_to_array(line, ',') AS cols
FROM lines
WHERE ord > 1   -- 跳过表头行
  )
INSERT INTO ems.cfg_envs (id, name, domain_id)
SELECT
  cols[1]::bigint  AS id,      -- CSV 的 id，与 profile 中 project 值一致
  cols[3]          AS name,     -- 显示名，如「本专科」「微专业」
  domain_id
FROM parsed
  ON CONFLICT (id) DO UPDATE
                        SET name = EXCLUDED.name,
                        domain_id = EXCLUDED.domain_id;

drop table ems.usr_profiles_properties;

create table ems.usr_roles_envs (role_id integer not null, env_id bigint not null);
create table ems.cfg_apps_envs (app_id integer not null, env_id bigint not null);
alter table ems.se_func_permissions add env_ids jsonb;

alter table ems.usr_roots add domain_id int4;
update ems.usr_roots r set domain_id=(select app.domain_id from ems.cfg_apps app where app.id=r.app_id);
delete from ems.usr_roots a where exists(select * from ems.usr_roots b where b.user_id=a.user_id and b.domain_id=a.domain_id and a.id > b.id);
alter table ems.usr_roots drop column app_id;
