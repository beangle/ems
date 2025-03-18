create table ems.oa_flow_active_processes (id bigint not null, start_at timestamptz not null, flow_id bigint not null, business_key varchar(255) not null);
create table ems.oa_flow_active_tasks (name varchar(255) not null, process_id bigint not null, id bigint not null, start_at timestamptz not null, idx integer default 0 not null, assignee_id bigint, due_time timestamptz);
create table ems.oa_flow_active_tasks_candidates (flow_active_task_id bigint not null, user_id bigint not null);
create table ems.oa_flow_activities (name varchar(255) not null, assignee varchar(50), candidates varchar(250), id bigint not null, remark varchar(255), flow_id bigint not null, idx integer default 0 not null, depart varchar(50));
create table ems.oa_flow_activities_groups (flow_activity_id bigint not null, group_id integer not null);
create table ems.oa_flow_attachments (name varchar(300) not null, task_id bigint not null, id bigint not null, file_size bigint default 0 not null, file_path varchar(500) not null);
create table ems.oa_flow_comments (messages varchar(4000) not null, updated_at timestamptz not null, task_id bigint not null, id bigint not null, user_id bigint not null);
create table ems.oa_flow_processes (env_json varchar(255) not null, id bigint not null, start_at timestamptz not null, end_at timestamptz, business_key varchar(255) not null, flow_id bigint not null, status integer not null);
create table ems.oa_flow_tasks (name varchar(255) not null, assignee_id bigint, process_id bigint not null, id bigint not null, status integer not null, idx integer default 0 not null, start_at timestamptz not null, data_json varchar(255) not null, end_at timestamptz);
create table ems.oa_flows (env_json varchar(2000) not null, flow_json varchar(8000) not null, code varchar(255) not null, id bigint not null, remark varchar(255), guard_json varchar(300) not null, business_id bigint not null, profile_id varchar(255) not null, name varchar(255) not null, updated_at timestamptz not null, domain_id integer not null);

--add departs
create table ems.usr_departs (code varchar(20) not null, id integer not null, indexno varchar(20) not null, parent_id integer, name varchar(300) not null, updated_at timestamptz not null, org_id integer not null, short_name varchar(200),begin_on date not null,end_on date);
alter table ems.usr_users add depart_id int4;
alter table ems.usr_departs add constraint pk_lqtwc4myrcgfw4e69xam0gx17 primary key (id);

