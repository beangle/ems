alter schema blob rename to blb;
alter schema session rename to ssn;
alter table blb.blob_metas rename column size to file_size;
alter table blb.blob_metas rename column path to file_path ;
alter table blb.blob_metas add constraint fk_8fkx493rrdnako4p08rxfg43t foreign key (profile_id) references blb.profiles (id);
alter table blb.profiles rename column path to base;
alter table blb.profiles drop public_list cascade;
alter table bulletin.docs rename column size to file_size;
alter table bulletin.docs rename column path to file_path;
alter table bulletin.news rename column content to contents;
alter table bulletin.notices rename column content to contents;
alter table bulletin.sensitive_words rename column content to contents;
alter table bulletin.sensitive_words add column domain_id int4;
update bulletin.sensitive_words set domain_id =(select min(id) from cfg.domains);

alter table se.data_resources rename column "scope" to scope_;
alter table se.func_resources rename column "scope" to scope_;
alter table usr.avatars rename path to file_path;
alter table usr.dimensions rename column source to source_;

alter table usr.group_members rename column "member" to is_member;
alter table usr.group_members rename column granter to is_granter;
alter table usr.group_members rename column manager to is_manager;
alter table usr.messages rename column content to contents;
alter table usr.notifications rename column content to contents;
alter table usr.role_members rename column "member" to is_member;
alter table usr.role_members rename column granter to is_granter;
alter table usr.role_members rename column manager to is_manager;
alter table usr.todoes rename column content to contents;

alter table usr.user_profiles_properties rename column value to value_;

comment on table blb.blob_metas is '文件信息';
comment on table blb.profiles is '文件配置';
comment on table bulletin.docs_user_categories is '适用用户类别';
comment on table bulletin.notices_user_categories is '适用用户类别';
comment on table bulletin.sensitive_words is '敏感词汇';
comment on table cfg.app_groups is '应用分组';
comment on table cfg.dbs_properties is '数据源属性';
comment on table usr.accounts is '账户';
comment on table usr.password_configs is '密码配置';
alter table usr.groups rename to user_groups;
alter table usr.group_members add constraint fk_dak9rix2b3s3lmv7ftnooi346 foreign key (group_id) references usr.user_groups (id);
alter table usr.group_members drop constraint fk_rpgq4bl4kui39wk9mlkl26ib cascade;
