create table ems.cfg_rule_metas (business_id bigint not null, name varchar(50) not null, description varchar(500) not null, id bigint not null, title varchar(80) not null);
create table ems.cfg_rule_param_metas (name varchar(50) not null, description varchar(200) not null, id bigint not null, rule_meta_id bigint not null, title varchar(80) not null, data_type varchar(255) not null);
create table ems.cfg_rule_params (rule_id bigint not null, contents varchar(500) not null, id bigint not null, meta_id bigint not null);
create table ems.cfg_rules (profile_id varchar(255) not null, name varchar(255) not null, updated_at timestamptz not null, enabled boolean default false not null, id bigint not null, meta_id bigint not null);

alter table ems.cfg_rule_metas add constraint pk_luomjgmjug1xl0nepih2e6bp7 primary key (id);
alter table ems.cfg_rule_param_metas add constraint pk_72klsho25nxj48adlsbsvxw24 primary key (id);
alter table ems.cfg_rule_params add constraint pk_ohdu0agjno63noube9jnd03x2 primary key (id);
alter table ems.cfg_rules add constraint pk_nqb0kg717ty66bcdlbevgg40w primary key (id);

create index idx_a9rxcvrr8vs3dxx35pdq68s52 on ems.cfg_rule_param_metas (rule_meta_id);
create index idx_qkjt0l48d4fxolr2kc23abi0k on ems.cfg_rule_params (rule_id);

comment on column ems.cfg_rule_metas.business_id is '业务类型ID';
comment on column ems.cfg_rule_metas.description is '描述';
comment on column ems.cfg_rule_metas.id is '非业务主键:auto_increment';
comment on column ems.cfg_rule_metas.name is '名称';
comment on column ems.cfg_rule_metas.title is '标题';
comment on column ems.cfg_rule_param_metas.data_type is '数据类型';
comment on column ems.cfg_rule_param_metas.description is '描述';
comment on column ems.cfg_rule_param_metas.id is '非业务主键:auto_increment';
comment on column ems.cfg_rule_param_metas.name is '名称';
comment on column ems.cfg_rule_param_metas.rule_meta_id is '规则元数据ID';
comment on column ems.cfg_rule_param_metas.title is '标题';
comment on column ems.cfg_rule_params.contents is '值';
comment on column ems.cfg_rule_params.id is '非业务主键:auto_increment';
comment on column ems.cfg_rule_params.meta_id is '规则参数元数据ID';
comment on column ems.cfg_rule_params.rule_id is '规则ID';
comment on column ems.cfg_rules.enabled is '是否启用';
comment on column ems.cfg_rules.id is '非业务主键:auto_increment';
comment on column ems.cfg_rules.meta_id is '规则元数据ID';
comment on column ems.cfg_rules.name is '名称';
comment on column ems.cfg_rules.profile_id is '场景配置ID';
comment on column ems.cfg_rules.updated_at is '更新时间';

