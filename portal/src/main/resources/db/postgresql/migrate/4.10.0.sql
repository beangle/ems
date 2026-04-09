alter table ems.usr_group_members  drop column is_member;
alter table ems.usr_group_members  drop column is_granter;
alter table ems.usr_group_members  drop column is_manager;

create table ems.usr_groups_roles (group_id integer not null, role_id integer not null);
alter table ems.usr_groups_roles add constraint pk_sb7917p9v5v4yodepe7kflr3s primary key (group_id,role_id);

alter table ems.usr_groups add code varchar(50);
alter table ems.usr_groups drop column creator_id;
alter table ems.usr_groups add manager_id int8;
alter table ems.usr_users add group_id int4;
alter table ems.usr_groups add org_id int4;

