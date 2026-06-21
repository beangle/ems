alter table ems.blb_profiles drop column named_by_sha ;
alter table ems.blb_profiles drop column public_download;

insert into ems.cfg_app_types (id,name,title) values(3,'mobile-app','移动端应用');
update ems.se_func_resources set name ='/portal'||name where app_id in(select id from ems.cfg_apps where name ='platform-portal') and name not like '/portal/%' and (name like '/user%' or name like '/admin%');
update ems.cfg_apps set base='{webapp}' where name ='platform-portal';
