CREATE TABLE ems.se_session_infoes
(
    id character varying(200) NOT NULL,
    principal character varying(200),
    data bytea,
    description character varying(200),
    ip character varying(200),
    agent character varying(200),
    category_id int4,
    domain_id int4,
    os character varying(200),
    login_at timestamp without time zone,
    tti_seconds int4,
    last_access_at timestamp without time zone,
    CONSTRAINT session_infoes_pkey PRIMARY KEY (id)
);

insert into ems.cfg_orgs(id,code,name,short_name,www_url,logo_url) values(${schoolId},'1','组织名','简称','www.baidu.com','www.baidu.com');
insert into ms.cfg_domains(id,org_id,name,title,hostname,logo_url) values(${domainId},${schoolId},'sys-name','系统名称','www.baidu.com','www.baidu.com');

insert into ems.usr_categories(id,code,name,en_name,begin_on,updated_at,org_id)values(1,'1','教师','teacher',current_date-1,now(),${schoolId});
insert into ems.usr_categories(id,code,name,en_name,begin_on,updated_at,org_id)values(2,'2','学生','student',current_date-1,now(),${schoolId});
insert into ems.usr_categories(id,code,name,en_name,begin_on,updated_at,org_id)values(3,'3','管理人员','manager',current_date-1,now(),${schoolId});
insert into ems.usr_categories(id,code,name,en_name,begin_on,updated_at,org_id)values(4,'4','其他','other',current_date-1,now(),${schoolId});

insert into ems.usr_users(id,code,name,category_id,begin_on,updated_at,org_id) values(1,'root','imroot',3,current_date-1,now(),${schoolId});
insert into ems.usr_accounts(id,user_id,domain_id,updated_at,enabled,locked,password,begin_on,end_on,passwd_expired_on)
values(1,1,${domainId},now(),true,false,'123456',current_date-1,null,current_date +720);

insert into ems.cfg_app_types(id,name,title) values(1,'web-app','web应用');
insert into ems.cfg_app_types(id,name,title) values(2,'web-ws','web服务');

insert into ems.log_levels(id,name) values(1,'信息');
insert into ems.log_levels(id,name) values(2,'警告');
insert into ems.log_levels(id,name) values(3,'错误');

insert into ems.cfg_app_groups(id,name,title,indexno,domain_id) values(1,'platform','系统管理','9',{domainId});

insert into ems.cfg_apps(id,name,secret,app_type_id,url,enabled,base,indexno,title,group_id,domain_id,nav_style)
values(1,'platform-ws','platform-ws',2,'{webapp}/api/platform',true,'{webapp}/api/platform','9.1','平台服务',1,{domainId},null);

insert into ems.cfg_apps(id,name,secret,app_type_id,url,enabled,base,indexno,title,group_id,domain_id,nav_style)
values(2,'platform-cas','platform-cas',1,'{webapp}/cas',true,'{webapp}/cas','9.2','平台认证中心',1,{domainId},'adminlte');

insert into ems.cfg_apps(id,name,secret,app_type_id,url,enabled,base,indexno,title,group_id,domain_id,nav_style)
values(3,'platform-portal','platform-portal',1,'{webapp}/portal',true,'{webapp}/portal','9.3','平台门户',1,{domainId},'adminlte');

insert into ems.usr_roots(id,app_id,user_id,updated_at)
select next_id('usr.roots'),app.id,1,now() from cfg.apps app;
