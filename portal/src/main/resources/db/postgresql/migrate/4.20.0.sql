alter table ems.usr_role_members add env_ids jsonb default '[]';
alter table ems.usr_role_members alter env_ids set not null;

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
                                          code       varchar(100) NOT NULL,
                                          name       varchar(100) NOT NULL,
  domain_id  integer      NOT NULL,
  CONSTRAINT pk_cfg_envs PRIMARY KEY (id),
  CONSTRAINT fk_cfg_envs_domain FOREIGN KEY (domain_id)
  REFERENCES ems.cfg_domains (id),
  CONSTRAINT idx_env UNIQUE (domain_id, code)
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
INSERT INTO ems.cfg_envs (id, code, name, domain_id)
SELECT
  cols[1]::bigint  AS id,      -- CSV 的 id，与 profile 中 project 值一致
  cols[2]          as code,
  cols[3]          AS name,     -- 显示名，如「本专科」「微专业」
  domain_id
FROM parsed
  ON CONFLICT (id) DO UPDATE
                        SET name = EXCLUDED.name,
                        code = EXCLUDED.code,
                        domain_id = EXCLUDED.domain_id;

drop table ems.usr_profiles_properties;

alter table ems.cfg_apps add column env_ids jsonb  default '[]';
alter table ems.usr_roles add column env_ids jsonb default '[]';
alter table ems.usr_roles alter env_ids set not null;

create table ems.se_role_app_envs (id bigint not null, role_id integer not null, app_id integer not null, env_id bigint not null);

alter table ems.usr_roots add domain_id int4;
update ems.usr_roots r set domain_id=(select app.domain_id from ems.cfg_apps app where app.id=r.app_id);
alter table ems.usr_roots alter domain_id set not null;
delete from ems.usr_roots a where exists(select * from ems.usr_roots b where b.user_id=a.user_id and b.domain_id=a.domain_id and a.id > b.id);
alter table ems.usr_roots drop app_id cascade;

----
create table ems.cfg_channel_types (name varchar(255) not null, id integer not null, title varchar(255) not null);
create table ems.se_channels (channel_type_id integer not null, embed_mode integer not null, base varchar(200) not null, id integer not null, app_id integer not null,enabled bool not null);
insert into ems.cfg_channel_types(id,name,title) values(1,'pc','PC端');
insert into ems.cfg_channel_types(id,name,title) values(2,'mobile','移动端');

insert into ems.se_channels(id,app_id,channel_type_id,base,embed_mode,enabled) select id,id,1,base,2,enabled from ems.cfg_apps where base is not null and app_type_id=1;

alter table ems.se_menus add channel_id int4;
update ems.se_menus m set channel_id=(select c.id from ems.se_channels c where c.app_id=m.app_id);
alter table ems.se_menus alter channel_id set not null;
alter table ems.se_menus drop app_id cascade;
alter table ems.se_menus rename column fonticon to icon;
alter table ems.se_menus add column route varchar(300);
update ems.se_menus m set route = (select f.name||(case when m.params not null then '?'||m.params else '' end) from ems.se_func_resources f where f.id=m.entry_id) where m.entry_id is not null;
alter table ems.se_menus drop column params;
alter table ems.se_menus drop entry_id cascade;

--
drop table ems.cfg_app_types cascade;
drop table ems.se_app_permissions cascade;
drop table ems.usr_roles_properties cascade;
--
alter table ems.cfg_apps drop app_type_id cascade;
alter table ems.cfg_apps drop nav_style cascade;
--
drop index ems.idx_chjpjbdcov744gg5lq2de7rx3;
drop index ems.idx_fdatks2vdh1idedyu5f6fb55l;
create index idx_g4tn2er1x799kfa58qg0kocv5 on ems.se_menus (channel_id);
--
alter table ems.cfg_channel_types add constraint pk_saobwwmk5hgqt3nu8j8vab2q0 primary key (id);
alter table ems.cfg_envs add constraint pk_gcj0b58iw6hbkfywab8q7hwyx primary key (id);
alter table ems.se_channels add constraint pk_9irbt4dlq1jkdxn2ohcj10apc primary key (id);
alter table ems.se_role_app_envs add constraint pk_g5r31tly8t7ue77xpqfkgj0bp primary key (id);
alter table ems.usr_env_profiles add constraint pk_ki3y1mwsmmvnxm6bi3jancjer primary key (id);
alter table ems.cfg_envs add constraint fk_gf1a2eds6tiqs9oqo1la8rmyr foreign key (domain_id) references ems.cfg_domains (id);
alter table ems.se_channels add constraint fk_jfkwbm9ofx2b2jecqldi6jppx foreign key (app_id) references ems.cfg_apps (id);
alter table ems.se_channels add constraint fk_gp6gt3p8j0hjvi0opwnssno4r foreign key (channel_type_id) references ems.cfg_channel_types (id);
alter table ems.se_role_app_envs add constraint fk_pb6yiiog6q5e07wpe32tvxg7l foreign key (app_id) references ems.cfg_apps (id);
alter table ems.se_role_app_envs add constraint fk_ajt683q0i5okfyvk0wsys8y88 foreign key (role_id) references ems.usr_roles (id);
alter table ems.se_role_app_envs add constraint fk_3m040w8o8okmih8g0klccffkv foreign key (env_id) references ems.cfg_envs (id);
alter table ems.usr_env_profiles add constraint fk_obrss55hfor3l9f3ph12pelrw foreign key (domain_id) references ems.cfg_domains (id);
alter table ems.usr_env_profiles add constraint fk_dm42nbpuw5728ep7ikfa4vuhv foreign key (env_id) references ems.cfg_envs (id);
alter table ems.usr_env_profiles add constraint fk_qf39xwtsxgoq6uox3b9f0pauk foreign key (user_id) references ems.usr_users (id);
alter table ems.cfg_apps drop constraint if exists fk_oyclhhl4b5hmp6irr0gcs9xbx cascade;
alter table ems.se_menus drop constraint if exists fk_9j0v9w5h4bij1ajoh8febi4wh cascade;
alter table ems.se_menus drop constraint if exists fk_m6o2fdnpl1ngm724sdy6gmptb cascade;
alter table ems.se_menus add constraint fk_gpx2y07rsdc8qs6smlkd9kjq2 foreign key (channel_id) references ems.se_channels (id);
alter table ems.usr_roots drop constraint if exists fk_akxow8uv93shp0ukqwgnqjs99 cascade;
alter table ems.usr_roots add constraint fk_ptys3k07lov27kt9u7cr8phal foreign key (domain_id) references ems.cfg_domains (id);
alter table ems.cfg_channel_types add constraint uk_gnj63a7uob97olnwp5vj5d7u unique (name);
alter table ems.cfg_envs add constraint idx_env unique (domain_id,code);
alter table ems.se_channels add constraint idx_channel unique (app_id,channel_type_id);
alter table ems.se_role_app_envs add constraint idx_role_app_env unique (role_id,app_id,env_id);
alter table ems.usr_env_profiles add constraint idx_user_profile unique (user_id,domain_id,env_id);
--
omment on table ems.cfg_channel_types is '前端类型';
comment on column ems.cfg_channel_types.id is '非业务主键:auto_increment';
comment on column ems.cfg_channel_types.name is '名称';
comment on column ems.cfg_channel_types.title is '标题';
comment on table ems.cfg_envs is '业务场景';
comment on column ems.cfg_envs.id is '非业务主键:auto_increment';
comment on column ems.cfg_envs.code is '代码';
comment on column ems.cfg_envs.domain_id is '业务系统ID';
comment on column ems.cfg_envs.name is '名称';
comment on table ems.se_channels is '菜单配置';
comment on column ems.se_channels.id is '非业务主键:auto_increment';
comment on column ems.se_channels.app_id is '应用ID';
comment on column ems.se_channels.base is '上下文地址';
comment on column ems.se_channels.channel_type_id is '前端类型ID';
comment on column ems.se_channels.embed_mode is '嵌入方式';
comment on column ems.se_channels.enabled is '是否启用';
comment on table ems.se_role_app_envs is '角色应用场景';
comment on column ems.se_role_app_envs.id is '非业务主键:auto_increment';
comment on column ems.se_role_app_envs.app_id is '应用ID';
comment on column ems.se_role_app_envs.env_id is '业务场景ID';
comment on column ems.se_role_app_envs.role_id is '角色ID';
comment on table ems.usr_env_profiles is '用户场景配置';
comment on column ems.usr_env_profiles.id is '非业务主键:auto_increment';
comment on column ems.usr_env_profiles.domain_id is '业务系统ID';
comment on column ems.usr_env_profiles.env_id is '业务场景ID';
comment on column ems.usr_env_profiles.properties is '属性';
comment on column ems.usr_env_profiles.user_id is '用户ID';
comment on column ems.cfg_apps.env_ids is '业务场景';
comment on table ems.cfg_third_party_apps is '第三方应用';
comment on table ems.jb_cron_task_logs is '计划任务日志';
comment on table ems.jb_cron_tasks is '计划任务';
comment on table ems.oa_done_todoes is '已办';
comment on table ems.oa_flow_active_processes is '活动流程';
comment on table ems.oa_flow_active_tasks is '活动任务';
comment on table ems.oa_flow_active_tasks_assignees is '办理人';
comment on table ems.oa_flow_activities is '流程活动';
comment on table ems.oa_flow_activities_groups is '受理用户组';
comment on table ems.oa_flow_attachments is '流程附件';
comment on table ems.oa_flow_comments is '流程意见';
comment on table ems.oa_flow_processes is '流程实例';
comment on table ems.oa_flow_tasks is '流程任务';
comment on table ems.oa_flows is '工作流';
comment on table ems.oa_message_templates is '消息模板';
comment on table ems.oa_notice_attachments is '公告附件';
comment on table ems.oa_signatures is '签名';
comment on column ems.se_menus.channel_id is '菜单配置ID';
comment on column ems.se_menus.icon is '图标';
comment on column ems.se_menus.route is '路由';
comment on table ems.se_oauth_tokens is 'OAuth令牌';
comment on table ems.usr_departs is '部门';
comment on column ems.usr_role_members.env_ids is '业务场景';
comment on column ems.usr_roles.env_ids is '业务场景';
comment on column ems.usr_roots.domain_id is '业务系统ID';
comment on table log.ems_error_logs is '错误日志';

