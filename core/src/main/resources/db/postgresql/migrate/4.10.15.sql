create table ems.oa_flow_gateways (id bigint not null, conditions varchar(4000) not null, flow_id bigint not null);
create table ems.oa_flow_tasks (name varchar(255) not null, id bigint not null, remark varchar(255), flow_id bigint not null, next_node varchar(255), idx integer default 0 not null, group_id integer not null);
create table ems.oa_flows (business_id bigint not null, domain_id integer not null, code varchar(255) not null, id bigint not null, name varchar(255) not null, updated_at timestamptz not null, remark varchar(255), data_json varchar(2000) not null);

alter table ems.oa_flow_gateways add constraint pk_3eot2v3uak0kk51f4u7obsr7a primary key (id);
alter table ems.oa_flow_tasks add constraint pk_ix1l2ynxneogn9xxg9poq7q0k primary key (id);
alter table ems.oa_flows add constraint pk_a73xr38hwrn5eq0cms9vtaojj primary key (id);

--add departs
create table ems.usr_departs (code varchar(20) not null, id integer not null, indexno varchar(20) not null, parent_id integer, name varchar(300) not null, updated_at timestamptz not null, org_id integer not null, short_name varchar(200));
alter table ems.usr_users add depart_id int4;
alter table ems.usr_departs add constraint pk_lqtwc4myrcgfw4e69xam0gx17 primary key (id);

