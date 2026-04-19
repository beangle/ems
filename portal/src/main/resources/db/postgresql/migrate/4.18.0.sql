alter table ems.oa_docs add embedded bool default false;
update ems.oa_docs d set embedded =true where exists(select * from ems.oa_notices_docs nd where nd.doc_id=d.id);

