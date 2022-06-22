create table log.levels (name varchar(255) not null, id integer not null);
create table log.session_events (username varchar(100) not null, domain_id integer not null, id bigint not null, principal varchar(100) not null, detail varchar(1000) not null, event_type integer not null, name varchar(100) not null, ip varchar(255) not null, updated_at timestamp not null);

alter table log.business_logs add constraint pk_e7nfc0kcwyw2dvnpa3ik7dryx primary key (id);
alter table log.levels add constraint pk_fagiox162rmboh86j3wvglkic primary key (id);

alter table ssn.session_events set schema log;
alter table log.business_logs add constraint fk_mmndjgdrq4yk8r6cfpn9v8bh6 foreign key (app_id) references cfg.apps (id);
alter table log.business_logs add constraint fk_sjqwybis3f1lherr191kkp7w5 foreign key (level_id) references log.levels (id);
