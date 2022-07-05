alter table se.menus rename name to en_name;
alter table se.menus rename title to name;
alter table se.menus add fonticon varchar(100);

alter table cfg.apps add en_title varchar(100);
update cfg.apps set en_title=title;
alter table cfg.apps alter column en_title set not null;

alter table cfg.app_groups add en_title varchar(100);
update cfg.app_groups set en_title=title;
alter table cfg.app_groups alter column en_title set not null;

alter table cfg.domains add en_title varchar(100);
update cfg.domains set en_title=title;
alter table cfg.domains alter column en_title set not null;
