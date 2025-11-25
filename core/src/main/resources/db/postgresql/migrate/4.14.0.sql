create table log.ems_error_logs (username varchar(100), exception_name varchar(400) not null, params varchar(4000), stack_trace varchar(4000) not null, id bigint not null, occurred_at timestamptz not null, app_id integer not null, message varchar(400) not null, request_url varchar(100) not null);

alter table log.ems_error_logs add constraint pk_5n4ch46jn9kkurc26ykg20hbw primary key (id);
