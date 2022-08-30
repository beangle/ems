
alter table bulletin.notices add issuer varchar(40);
update bulletin.notices n set issuer = (select u.name from usr.users u where u.id=n.operator_id);
alter table bulletin.notice alter column issuer set not null;
