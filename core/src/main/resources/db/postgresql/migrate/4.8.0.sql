--增加了主题配置
create table ems.cfg_themes (domain_id integer not null, enabled boolean default false not null,primary_color varchar(15) not null, grid_border_color varchar(15) not null, id bigint not null, gridbar_bg_color varchar(15) not null, search_bg_color varchar(15) not null, name varchar(255) not null, navbar_bg_color varchar(15) not null);
alter table ems.cfg_themes add constraint pk_8cgpd1jxx29su0g416k4uqq7h primary key (id);

alter table ems.cfg_portalets add domain_id integer;
--careful for multiple domain
update ems.cfg_portalets set domain_id = (select id from ems.cfg_domains);
alter table ems.cfg_portalets alter column domain_id set not null;


