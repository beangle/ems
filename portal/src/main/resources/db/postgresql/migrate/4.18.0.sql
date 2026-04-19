alter table ems.oa_docs add notice_id  bigint;
update ems.oa_docs  d set notice_id=(select nd.notice_id from ems.oa_notices_docs nd where nd.doc_id=d.id );

drop table ems.oa_notices_docs;

create table ems.oa_notice_attachments (embedded boolean default false not null, id bigint not null,
                                        notice_id bigint not null, file_size integer default 0 not null,
                                        file_path varchar(255) not null, name varchar(255) not null);

alter table ems.oa_notice_attachments add constraint pk_4ixhtw4naipbw9wyvufbpdgf5 primary key (id);
create index idx_ih905m5f9dsqkmy3xpl8b30yn on ems.oa_notice_attachments (notice_id);

insert into ems.oa_notice_attachments(id,embedded,notice_id,file_size,file_path,name)
select id,false,notice_id,file_size,file_path,name from ems.oa_docs where notice_id is not null;

delete from ems.oa_docs_categories where doc_id in (select id from ems.oa_docs where notice_id is not null);
delete from ems.oa_docs where notice_id is not null;
