alter schema bulletin rename to oa;
alter table cfg.app_groups add short_title varchar(10);
update cfg.app_groups set short_title=substr(title,1,2);
alter table cfg.app_groups alter column short_title set not null;

update se.func_resources set name = replace(name,'/bulletin/','/oa/');

create table cfg.portalets(name varchar(255) not null, url varchar(255) not null, enabled boolean not null, id integer not null, using_iframe boolean not null, title varchar(255) not null, col_span integer not null);
create table cfg.portalets_categories(portalet_id integer not null, user_category_id integer not null);

alter table usr.messages set schema oa;

