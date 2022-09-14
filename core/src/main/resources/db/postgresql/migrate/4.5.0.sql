alter schema bulletin rename to oa;
alter table cfg.app_groups add short_title varchar(10);
update cfg.app_groups set short_title=substr(title,1,2);
alter table cfg.app_groups alter column short_title set not null;

update se.func_resources set name = replace(name,'/bulletin/','/oa/');

