create table ssn.session_infoes(id varchar(200),data bytea,principal varchar(200),description varchar(200),ip varchar(60),agent varchar(200),os varchar(200),login_at timestamp,last_access_at timestamp,category_id int4);
alter table ssn.session_infoes add primary key (id);

insert into usr.user_categories(id,code,name,en_name,begin_on,updated_at,org_id)values(1,'1','教师','teacher',current_date-1,now(),${schoolId});
insert into usr.user_categories(id,code,name,en_name,begin_on,updated_at,org_id)values(2,'2','学生','student',current_date-1,now(),${schoolId});
insert into usr.user_categories(id,code,name,en_name,begin_on,updated_at,org_id)values(3,'3','管理人员','manager',current_date-1,now(),${schoolId});
insert into usr.user_categories(id,code,name,en_name,begin_on,updated_at,org_id)values(4,'4','其他','other',current_date-1,now(),${schoolId});

insert into usr.users(id,code,name,category_id,begin_on,updated_at,org_id) values(1,'root','imroot',3,current_date-1,now(),${schoolId});
insert into usr.accounts(id,user_id,domain_id,updated_at,enabled,locked,password,begin_on,end_on,passwd_expired_on)
values(1,1,${schoolId},now(),true,false,'123456',current_date-1,null,current_date +720);

insert into cfg.app_types(id,name,title) values(1,'web-app','web应用');
insert into cfg.app_types(id,name,title) values(2,'web-ws','web服务');

insert into log.levels(id,name) values(1,'信息');
insert into log.levels(id,name) values(2,'警告');
insert into log.levels(id,name) values(3,'错误');
