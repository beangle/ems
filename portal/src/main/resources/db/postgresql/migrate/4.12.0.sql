alter table log.ems_business_logs rename level_id to log_level;
drop table ems.log_levels cascade;
