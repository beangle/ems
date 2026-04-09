create schema log;
alter table ems.log_business_logs set schema log;
alter table ems.log_session_events set schema log;

alter table log.log_business_logs rename to ems_business_logs;
alter table log.log_session_events rename to ems_session_events;
