create schema log;
create table log.levels (name varchar(255) not null, id integer not null);

create table log.business_logs (ip varchar(50) not null, entry varchar(100) not null, operate_at timestamp not null, id bigint not null, operator varchar(200) not null, summary varchar(500) not null, level_id integer not null, agent varchar(100) not null, resources varchar(300) not null, details varchar(2000) not null, app_id integer not null);

alter table log.business_logs add constraint pk_e7nfc0kcwyw2dvnpa3ik7dryx primary key (id);
alter table log.levels add constraint pk_fagiox162rmboh86j3wvglkic primary key (id);

alter table ssn.session_events set schema log;
alter table log.business_logs add constraint fk_mmndjgdrq4yk8r6cfpn9v8bh6 foreign key (app_id) references cfg.apps (id);
alter table log.business_logs add constraint fk_sjqwybis3f1lherr191kkp7w5 foreign key (level_id) references log.levels (id);
insert into log.levels(id,name) values(1,'信息');
insert into log.levels(id,name) values(2,'警告');
insert into log.levels(id,name) values(3,'错误');

alter table cfg.domains add logo_url varchar(300);
update cfg.domains d set logo_url=(select o.logo_url from cfg.orgs o where o.id=d.org_id);
update cfg.apps set nav_style ='adminlte' where nav_style is not null;
