alter table ems.usr_roots add column begin_on date not null default current_date;
alter table ems.usr_roots alter column begin_on drop default;
alter table ems.usr_roots add column end_on date;
comment on column ems.usr_roots.begin_on is '生效日期';
comment on column ems.usr_roots.end_on is '失效日期';
