alter table ems.oa_flow_activities add guard varchar(500);
alter table ems.oa_flow_activities add guard_comment varchar(100);
create table ems.oa_signatures (id bigint not null, file_size integer default 0 not null, file_path varchar(255) not null, user_id bigint not null, media_type varchar(255) not null, updated_at timestamptz not null);
