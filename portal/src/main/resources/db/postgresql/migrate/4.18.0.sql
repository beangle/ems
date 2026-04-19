alter table ems.oa_docs add notice_id  bigint;
update ems.oa_docs  d set notice_id=(select nd.notice_id from ems.oa_notices_docs nd where nd.doc_id=d.id );

drop table ems.oa_notices_docs;
