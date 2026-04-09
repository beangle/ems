create table cfg.templates (id bigint not null, file_size integer not null, file_path varchar(255) not null, app_id integer not null, media_type varchar(255) not null, name varchar(255) not null, updated_at timestamp not null);
create table cfg.reconfigs (updated_at timestamp not null, contents varchar(1000) not null, id bigint not null, remark varchar(255), app_id integer not null);
alter table cfg.reconfigs add constraint pk_rvl6mq7go4b0c2p62hhqgptt9 primary key (id);
alter table cfg.reconfigs add constraint uk_d10ukx97xjnd55rpxc2ft8n3u unique (app_id);
alter table cfg.templates add constraint pk_tn6q8qe5k2nn1buqoxagrxnbg primary key (id);
alter table cfg.templates add constraint uk_grb4brw28cd2iar8ehk5y4g9v unique (app_id,name);

