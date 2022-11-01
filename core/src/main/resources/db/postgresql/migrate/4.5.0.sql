alter schema bulletin rename to oa;
alter table cfg.app_groups add short_title varchar(10);
update cfg.app_groups set short_title=substr(title,1,2);
alter table cfg.app_groups alter column short_title set not null;

update se.func_resources set name = replace(name,'/bulletin/','/oa/');
alter table usr.messages set schema oa;
alter table usr.todoes set schema oa;
alter table usr.notifications set schema oa;

create table cfg.portalets (name varchar(255) not null, row_index integer not null, url varchar(255) not null,
                            enabled boolean not null, id integer not null, title varchar(255) not null,
                            colspan integer not null, using_iframe boolean not null, idx integer not null);
create table cfg.portalets_categories (portalet_id integer not null, user_category_id integer not null);
alter table cfg.portalets add constraint pk_d9aofqmhlpgu2yt4gvxmylsxv primary key (id);
alter table cfg.portalets_categories add constraint pk_9xkrjhk1kq7o3qp67vwc4qi2b primary key (portalet_id,user_category_id);

insert into cfg.portalets(id,name,title,row_index,colspan,url,using_iframe,idx,enabled)
                   values(1,'platform-notice','通知公告',1,6,'/portal/index/noticePortalet',false,1,true);
insert into cfg.portalets(id,name,title,row_index,colspan,url,using_iframe,idx,enabled)
                   values(2,'platform-doc','文档下载',1,6,'/portal/index/docPortalet',false,2,true);
insert into cfg.portalets_categories(portalet_id,user_category_id) select p.id,uc.id from cfg.portalets p,usr.user_categories uc;



