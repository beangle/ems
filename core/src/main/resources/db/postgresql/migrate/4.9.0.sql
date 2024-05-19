alter table ems.usr_users add  password varchar(200);
alter table ems.usr_users add  passwd_expired_on date;
alter table ems.usr_users add  enabled bool default true;
alter table ems.usr_users add  locked bool default false;
update ems.usr_users u set (password,passwd_expired_on,enabled,locked)=
(select  a.password,a.passwd_expired_on,a.enabled,a.locked from ems.usr_accounts a where a.user_id=u.id);

select * from ems.usr_users u where
(select count(*) from ems.usr_accounts a where a.user_id=u.id )>1;
