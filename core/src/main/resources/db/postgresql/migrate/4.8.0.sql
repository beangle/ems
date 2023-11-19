--增加了主题配置
create table ems.cfg_themes (domain_id integer not null, enabled boolean default false not null,primary_color varchar(15) not null, grid_border_color varchar(15) not null, id bigint not null, gridbar_bg_color varchar(15) not null, search_bg_color varchar(15) not null, name varchar(255) not null, navbar_bg_color varchar(15) not null);
create table ems.cfg_text_bundles (name varchar(255) not null, texts text not null, locale varchar(255) not null, id bigint not null, app_id integer not null);
alter table ems.cfg_themes add constraint pk_8cgpd1jxx29su0g416k4uqq7h primary key (id);
alter table ems.cfg_text_bundles add constraint pk_7ckmfs80yl3vy0rb4h5kw0wy7 primary key (id);

alter table ems.usr_users add mobile varchar(15);
alter table ems.usr_users add email varchar(100);
alter table ems.cfg_portalets add domain_id integer;
--careful for multiple domain
update ems.cfg_portalets set domain_id = (select id from ems.cfg_domains);
alter table ems.cfg_portalets alter column domain_id set not null;

