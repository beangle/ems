create table ems.jb_cron_task_logs (status_code integer default 0 not null, duration bigint not null, task_id bigint not null, id bigint not null, execute_at timestamptz not null, result_file_path varchar(500) not null);
create table ems.jb_cron_tasks (status_code integer, duration bigint, description varchar(500) not null, domain_id integer not null, enabled boolean default false not null, last_execute_at timestamptz, id bigint not null, expression varchar(100) not null, target varchar(500) not null, name varchar(100) not null, updated_at timestamptz not null, command varchar(500) not null);
alter table ems.jb_cron_task_logs add constraint pk_11be0fdugwgb8ikd3di6piful primary key (id);
alter table ems.jb_cron_tasks add constraint idx_cron_task unique (domain_id,name);
alter table ems.jb_cron_tasks add constraint pk_ekeyej7cs8cyenoef6fjv6qo8 primary key (id);
