alter table ems.oa_flow_activities add guard varchar(500);
alter table ems.oa_flow_activities add guard_comment varchar(100);
create table ems.oa_signatures (id bigint not null, file_size integer default 0 not null, file_path varchar(255) not null, user_id bigint not null, media_type varchar(255) not null, updated_at timestamptz not null);

alter table ems.oa_flows add form_url varchar(255);
alter table ems.oa_todoes add url varchar(255);
alter table ems.oa_todoes add business_id int8;
alter table ems.oa_todoes add business_key varchar(255);
alter table ems.oa_todoes add done bool default false;

create index idx_7wu1nir9kwrxh80w8l9jmvubv on ems.oa_todoes (user_id);

create table ems.oa_done_todoes (updated_at timestamptz not null, url varchar(255) not null, domain_id integer not null, contents varchar(255) not null, id bigint not null, business_key varchar(255) not null, user_id bigint not null, business_id bigint not null, complete_at timestamptz not null);
alter table ems.oa_done_todoes add constraint pk_mn26km960qlp4vdudumww56lb primary key (id);
create index idx_qg5tig2qcku0ai7jqajjre0xj on ems.oa_done_todoes (user_id);

create index idx_o2hh6gu01jr2nukdqh14g4vob on ems.oa_notifications (recipient_id);
drop table ems.oa_notifications;

alter table ems.oa_messages alter column sender_id drop not null;
alter table ems.oa_messages add send_from varchar(200);
update ems.oa_messages msg set send_from=(select u.name from ems.usr_users u where u.id=msg.sender_id);

