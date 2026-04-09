create table ems.cfg_third_party_apps (secret varchar(100) not null, domain_id integer not null, code varchar(50) not null, id bigint not null, name varchar(100) not null, updated_at timestamptz not null);
alter table ems.cfg_third_party_apps add constraint pk_rrfs9bcq1gl1dhw3894kitrn5 primary key (id);
alter table ems.cfg_third_party_apps add constraint fk_ltk3pyvq43p809xogsm0axum2 foreign key (domain_id) references ems.cfg_domains (id);

