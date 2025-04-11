alter table ems.oa_flow_activities add guard varchar(500);
alter table ems.oa_flow_activities add guard_comment varchar(100);
create table ems.oa_signatures (id bigint not null, file_size integer default 0 not null, file_path varchar(255) not null, user_id bigint not null, media_type varchar(255) not null, updated_at timestamptz not null);

alter table ems.oa_flows add form_url varchar(255);
alter table ems.oa_flows add todo_message_id bigint;
alter table ems.oa_flows add result_message_id bigint;

alter table ems.oa_todoes add url varchar(255);
alter table ems.oa_todoes add title varchar(200);
alter table ems.oa_todoes add business_id int8;
alter table ems.oa_todoes add business_key varchar(255);
alter table ems.oa_todoes add done bool default false;
alter table ems.oa_todoes add sms_status int4 default 0;

create index idx_7wu1nir9kwrxh80w8l9jmvubv on ems.oa_todoes (user_id);

create table ems.oa_done_todoes (updated_at timestamptz not null, url varchar(300) not null, domain_id integer not null, contents varchar(1000) not null, id bigint not null, business_key varchar(100) not null, title varchar(200) not null, user_id bigint not null, business_id bigint not null, complete_at timestamptz not null);
alter table ems.oa_done_todoes add constraint pk_mn26km960qlp4vdudumww56lb primary key (id);
create index idx_qg5tig2qcku0ai7jqajjre0xj on ems.oa_done_todoes (user_id);

drop table ems.oa_notifications;

alter table ems.oa_messages alter column sender_id drop not null;
alter table ems.oa_messages add send_from varchar(200);
update ems.oa_messages msg set send_from=(select u.name from ems.usr_users u where u.id=msg.sender_id);

--add flow messages
create table ems.oa_message_templates (business_id bigint not null, delay_minutes integer default 0 not null, variables varchar(255), contents varchar(3000) not null, id bigint not null, todo boolean default false not null, title varchar(400) not null, name varchar(255) not null, updated_at timestamptz not null);
alter table ems.oa_message_templates add constraint pk_ns2rk4q0gl4ls5ctn6j6j1b38 primary key (id);
