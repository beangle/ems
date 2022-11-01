comment on column ems.blb_blob_metas.domain_id is '业务系统ID';
comment on column ems.blb_blob_metas.file_path is '文件路径';
comment on column ems.blb_blob_metas.file_size is '大小';
comment on column ems.blb_blob_metas.id is '非业务主键:datetime';
comment on column ems.blb_blob_metas.media_type is '文件类型';
comment on column ems.blb_blob_metas.name is '名称';
comment on column ems.blb_blob_metas.owner is '所有者';
comment on column ems.blb_blob_metas.profile_id is '文件配置ID';
comment on column ems.blb_blob_metas.sha is 'Sha';
comment on column ems.blb_blob_metas.updated_at is '更新时间';
comment on column ems.blb_profiles.base is '基础路径';
comment on column ems.blb_profiles.domain_id is '业务系统ID';
comment on column ems.blb_profiles.id is '非业务主键:auto_increment';
comment on column ems.blb_profiles.name is '名称';
comment on column ems.blb_profiles.named_by_sha is '按照sha命名';
comment on column ems.blb_profiles.public_download is '公开下载';
comment on column ems.blb_profiles.users is '用户名';
comment on column ems.cfg_app_groups.domain_id is '业务系统ID';
comment on column ems.cfg_app_groups.en_title is '英文标题';
comment on column ems.cfg_app_groups.id is '非业务主键:auto_increment';
comment on column ems.cfg_app_groups.indexno is '顺序号';
comment on column ems.cfg_app_groups.name is '名称';
comment on column ems.cfg_app_groups.short_title is '简称';
comment on column ems.cfg_app_groups.title is '标题';
comment on column ems.cfg_app_types.id is '非业务主键:auto_increment';
comment on column ems.cfg_app_types.name is '名称';
comment on column ems.cfg_app_types.title is '标题';
comment on column ems.cfg_apps.app_type_id is '应用类型ID';
comment on column ems.cfg_apps.base is '上下文';
comment on column ems.cfg_apps.domain_id is '业务系统ID';
comment on column ems.cfg_apps.en_title is '英文标题';
comment on column ems.cfg_apps.enabled is '是否启用';
comment on column ems.cfg_apps.group_id is '应用分组ID';
comment on column ems.cfg_apps.id is '非业务主键:auto_increment';
comment on column ems.cfg_apps.indexno is '排序索引号';
comment on column ems.cfg_apps.logo_url is '应用LOGO';
comment on column ems.cfg_apps.name is '应用名称';
comment on column ems.cfg_apps.nav_style is '导航风格';
comment on column ems.cfg_apps.remark is '备注';
comment on column ems.cfg_apps.secret is '密钥';
comment on column ems.cfg_apps.title is '标题';
comment on column ems.cfg_apps.url is '应用地址';
comment on column ems.cfg_credentials.domain_id is '业务系统ID';
comment on column ems.cfg_credentials.expired_at is '过期时间';
comment on column ems.cfg_credentials.id is '非业务主键:auto_increment';
comment on column ems.cfg_credentials.name is '名称';
comment on column ems.cfg_credentials.password is '密码';
comment on column ems.cfg_credentials.updated_at is '更新时间';
comment on column ems.cfg_credentials.username is '用户名';
comment on column ems.cfg_data_sources.app_id is '应用ID';
comment on column ems.cfg_data_sources.credential_id is '凭证ID';
comment on column ems.cfg_data_sources.db_id is '数据库ID';
comment on column ems.cfg_data_sources.id is '非业务主键:auto_increment';
comment on column ems.cfg_data_sources.maximum_pool_size is '最大连接数';
comment on column ems.cfg_data_sources.name is '名称';
comment on column ems.cfg_data_sources.remark is '备注';
comment on column ems.cfg_dbs.database_name is '数据库名';
comment on column ems.cfg_dbs.domain_id is '业务系统ID';
comment on column ems.cfg_dbs.driver is '驱动';
comment on column ems.cfg_dbs.id is '非业务主键:auto_increment';
comment on column ems.cfg_dbs.name is '名称';
comment on column ems.cfg_dbs.port_number is '端口';
comment on column ems.cfg_dbs.remark is '备注';
comment on column ems.cfg_dbs.server_name is '服务器地址';
comment on column ems.cfg_dbs.url is 'Java数据库连接字符串';
comment on column ems.cfg_dbs_properties.db_id is '数据库ID';
comment on column ems.cfg_dbs_properties.name is 'name';
comment on column ems.cfg_dbs_properties.value_ is '数据源属性';
comment on column ems.cfg_domains.en_title is '英文标题';
comment on column ems.cfg_domains.hostname is '主机域名';
comment on column ems.cfg_domains.id is '非业务主键:auto_increment';
comment on column ems.cfg_domains.logo_url is 'logo 网址';
comment on column ems.cfg_domains.name is '名称';
comment on column ems.cfg_domains.org_id is '组织ID';
comment on column ems.cfg_domains.sashub_base is 'SasHub的地址';
comment on column ems.cfg_domains.sashub_profile is 'SasHub上注册的key';
comment on column ems.cfg_domains.title is '标题';
comment on column ems.cfg_files.app_id is '应用ID';
comment on column ems.cfg_files.file_path is '路径';
comment on column ems.cfg_files.file_size is '文件大小';
comment on column ems.cfg_files.id is '非业务主键:auto_increment';
comment on column ems.cfg_files.media_type is '类型';
comment on column ems.cfg_files.name is '名称';
comment on column ems.cfg_files.updated_at is '更新时间';
comment on column ems.cfg_orgs.code is '代码';
comment on column ems.cfg_orgs.id is '非业务主键:auto_increment';
comment on column ems.cfg_orgs.logo_url is '组织Logo';
comment on column ems.cfg_orgs.name is '名称';
comment on column ems.cfg_orgs.remark is '备注';
comment on column ems.cfg_orgs.short_name is '简称';
comment on column ems.cfg_orgs.www_url is '网站地址';
comment on column ems.cfg_portalets.colspan is '列数';
comment on column ems.cfg_portalets.enabled is '是否启用';
comment on column ems.cfg_portalets.id is '非业务主键:auto_increment';
comment on column ems.cfg_portalets.idx is '序号';
comment on column ems.cfg_portalets.name is '名称';
comment on column ems.cfg_portalets.row_index is '行号';
comment on column ems.cfg_portalets.title is '标题';
comment on column ems.cfg_portalets.url is '地址';
comment on column ems.cfg_portalets.using_iframe is '是否支持iframe';
comment on column ems.cfg_portalets_categories.category_id is '用户类别ID';
comment on column ems.cfg_portalets_categories.portalet_id is '页面小部件ID';
comment on column ems.log_business_logs.agent is '操作客户端代理';
comment on column ems.log_business_logs.app_id is '应用ID';
comment on column ems.log_business_logs.details is '操作内容';
comment on column ems.log_business_logs.entry is '访问入口';
comment on column ems.log_business_logs.id is '非业务主键:datetime';
comment on column ems.log_business_logs.ip is 'IP';
comment on column ems.log_business_logs.level_id is '日志级别ID';
comment on column ems.log_business_logs.operate_at is '操作时间';
comment on column ems.log_business_logs.operator is '操作人';
comment on column ems.log_business_logs.resources is '对应的资源';
comment on column ems.log_business_logs.summary is '操作内容摘要';
comment on column ems.log_levels.id is '非业务主键:auto_increment';
comment on column ems.log_levels.name is '名称';
comment on column ems.log_session_events.detail is '明细';
comment on column ems.log_session_events.domain_id is '业务系统ID';
comment on column ems.log_session_events.event_type is '时间类型';
comment on column ems.log_session_events.id is '非业务主键:auto_increment';
comment on column ems.log_session_events.ip is 'IP';
comment on column ems.log_session_events.name is '名称';
comment on column ems.log_session_events.principal is '用户名';
comment on column ems.log_session_events.updated_at is '更新时间';
comment on column ems.log_session_events.username is '账户';
comment on column ems.oa_docs.app_id is '应用ID';
comment on column ems.oa_docs.archived is '是否归档';
comment on column ems.oa_docs.file_path is '路径';
comment on column ems.oa_docs.file_size is '大小';
comment on column ems.oa_docs.id is '非业务主键:datetime';
comment on column ems.oa_docs.name is '名称';
comment on column ems.oa_docs.updated_at is '更新时间';
comment on column ems.oa_docs.upload_by_id is '上传人ID';
comment on column ems.oa_docs_categories.category_id is '用户类别ID';
comment on column ems.oa_docs_categories.doc_id is '文档ID';
comment on column ems.oa_messages.contents is '内容';
comment on column ems.oa_messages.id is '非业务主键:datetime';
comment on column ems.oa_messages.recipient_id is '接受人ID';
comment on column ems.oa_messages.sender_id is '发送人ID';
comment on column ems.oa_messages.sent_at is '发送时间';
comment on column ems.oa_messages.status is '状态';
comment on column ems.oa_messages.title is '标题';
comment on column ems.oa_news.archived is '是否归档';
comment on column ems.oa_news.contents is '内容';
comment on column ems.oa_news.domain_id is '业务系统ID';
comment on column ems.oa_news.id is '非业务主键:datetime';
comment on column ems.oa_news.published_on is '发布日期';
comment on column ems.oa_news.title is '标题';
comment on column ems.oa_news.url is '链接地址';
comment on column ems.oa_notices.app_id is '应用ID';
comment on column ems.oa_notices.archived is '是否归档';
comment on column ems.oa_notices.auditor_id is '审核人ID';
comment on column ems.oa_notices.begin_on is '开始日期';
comment on column ems.oa_notices.contents is '内容';
comment on column ems.oa_notices.created_at is '创建时间';
comment on column ems.oa_notices.end_on is '结束日期';
comment on column ems.oa_notices.id is '非业务主键:datetime';
comment on column ems.oa_notices.issuer is '发布者';
comment on column ems.oa_notices.operator_id is '用户ID';
comment on column ems.oa_notices.popup is '是否弹窗';
comment on column ems.oa_notices.published_at is '发布日期';
comment on column ems.oa_notices.status is '状态';
comment on column ems.oa_notices.sticky is '是否置顶';
comment on column ems.oa_notices.title is '标题';
comment on column ems.oa_notices.updated_at is '更新时间';
comment on column ems.oa_notices_categories.category_id is '用户类别ID';
comment on column ems.oa_notices_categories.notice_id is '公告ID';
comment on column ems.oa_notices_docs.doc_id is '文档ID';
comment on column ems.oa_notices_docs.notice_id is '公告ID';
comment on column ems.oa_notifications.contents is '内容';
comment on column ems.oa_notifications.id is '非业务主键:datetime';
comment on column ems.oa_notifications.importance is '重要程度';
comment on column ems.oa_notifications.recipient_id is '接受人ID';
comment on column ems.oa_notifications.sent_at is '发送时间';
comment on column ems.oa_notifications.subject is '主题';
comment on column ems.oa_sensitive_words.contents is '敏感词汇列表';
comment on column ems.oa_sensitive_words.domain_id is '业务系统ID';
comment on column ems.oa_sensitive_words.id is '非业务主键:auto_increment';
comment on column ems.oa_todoes.contents is '内容';
comment on column ems.oa_todoes.domain_id is '业务系统ID';
comment on column ems.oa_todoes.id is '非业务主键:datetime';
comment on column ems.oa_todoes.updated_at is '更新时间';
comment on column ems.oa_todoes.user_id is '用户ID';
comment on column ems.se_app_permissions.actions is '操作';
comment on column ems.se_app_permissions.app_id is '应用ID';
comment on column ems.se_app_permissions.begin_at is '生效时间';
comment on column ems.se_app_permissions.end_at is '失效时间';
comment on column ems.se_app_permissions.id is '非业务主键:auto_increment';
comment on column ems.se_app_permissions.resource_id is '功能资源ID';
comment on column ems.se_app_permissions.restrictions is '限制条件';
comment on column ems.se_data_permissions.actions is '操作';
comment on column ems.se_data_permissions.app_id is '应用ID';
comment on column ems.se_data_permissions.attrs is '属性';
comment on column ems.se_data_permissions.begin_at is '开始时间';
comment on column ems.se_data_permissions.description is '描述';
comment on column ems.se_data_permissions.domain_id is '业务系统ID';
comment on column ems.se_data_permissions.end_at is '结束时间';
comment on column ems.se_data_permissions.filters is '过滤条件';
comment on column ems.se_data_permissions.func_resource_id is '功能资源ID';
comment on column ems.se_data_permissions.id is '非业务主键:auto_increment';
comment on column ems.se_data_permissions.remark is '备注';
comment on column ems.se_data_permissions.resource_id is '数据资源ID';
comment on column ems.se_data_permissions.restrictions is '限制条件';
comment on column ems.se_data_permissions.role_id is '角色ID';
comment on column ems.se_data_resources.actions is '操作';
comment on column ems.se_data_resources.domain_id is '业务系统ID';
comment on column ems.se_data_resources.id is '非业务主键:auto_increment';
comment on column ems.se_data_resources.name is '名称';
comment on column ems.se_data_resources.remark is '备注';
comment on column ems.se_data_resources.scope_ is '范围';
comment on column ems.se_data_resources.title is '标题';
comment on column ems.se_data_resources.type_name is '类型';
comment on column ems.se_func_permissions.actions is '操作';
comment on column ems.se_func_permissions.begin_at is '生效时间';
comment on column ems.se_func_permissions.end_at is '失效时间';
comment on column ems.se_func_permissions.id is '非业务主键:auto_increment';
comment on column ems.se_func_permissions.remark is '备注';
comment on column ems.se_func_permissions.resource_id is '功能资源ID';
comment on column ems.se_func_permissions.restrictions is '限制条件';
comment on column ems.se_func_permissions.role_id is '角色ID';
comment on column ems.se_func_resources.actions is '操作';
comment on column ems.se_func_resources.app_id is '应用ID';
comment on column ems.se_func_resources.enabled is '是否启用';
comment on column ems.se_func_resources.id is '非业务主键:auto_increment';
comment on column ems.se_func_resources.name is '名称';
comment on column ems.se_func_resources.remark is '备注';
comment on column ems.se_func_resources.scope_ is '范围';
comment on column ems.se_func_resources.title is '标题';
comment on column ems.se_menus.app_id is '应用ID';
comment on column ems.se_menus.en_name is '英文标题';
comment on column ems.se_menus.enabled is '是否启用';
comment on column ems.se_menus.entry_id is '功能资源ID';
comment on column ems.se_menus.fonticon is '字体图标';
comment on column ems.se_menus.id is '非业务主键:auto_increment';
comment on column ems.se_menus.indexno is '顺序号';
comment on column ems.se_menus.name is '名称';
comment on column ems.se_menus.params is '参数';
comment on column ems.se_menus.parent_id is '菜单ID';
comment on column ems.se_menus.remark is '备注';
comment on column ems.se_menus_resources.func_resource_id is '功能资源ID';
comment on column ems.se_menus_resources.menu_id is '菜单ID';
comment on column ems.se_session_configs.capacity is '最大容量';
comment on column ems.se_session_configs.category_id is '用户类别ID';
comment on column ems.se_session_configs.check_capacity is '是否检查容量';
comment on column ems.se_session_configs.check_concurrent is '是否检查多重会话';
comment on column ems.se_session_configs.concurrent is '多重会话数';
comment on column ems.se_session_configs.domain_id is '业务系统ID';
comment on column ems.se_session_configs.id is '非业务主键:auto_increment';
comment on column ems.se_session_configs.tti_minutes is '过期时间';
comment on column ems.usr_accounts.begin_on is '生效日期';
comment on column ems.usr_accounts.domain_id is '业务系统ID';
comment on column ems.usr_accounts.enabled is '是否启用';
comment on column ems.usr_accounts.end_on is '失效日期';
comment on column ems.usr_accounts.id is '非业务主键:auto_increment';
comment on column ems.usr_accounts.locked is '是否锁定';
comment on column ems.usr_accounts.passwd_expired_on is '密码过期日期';
comment on column ems.usr_accounts.password is '密码';
comment on column ems.usr_accounts.updated_at is '更新时间';
comment on column ems.usr_accounts.user_id is '用户ID';
comment on column ems.usr_avatars.file_name is '文件名';
comment on column ems.usr_avatars.file_path is '文件路径';
comment on column ems.usr_avatars.id is '非业务主键:assigned';
comment on column ems.usr_avatars.updated_at is '更新时间';
comment on column ems.usr_avatars.user_id is '用户ID';
comment on column ems.usr_categories.begin_on is '生效日期';
comment on column ems.usr_categories.code is '代码';
comment on column ems.usr_categories.en_name is '英文名';
comment on column ems.usr_categories.end_on is '失效日期';
comment on column ems.usr_categories.id is '非业务主键:auto_increment';
comment on column ems.usr_categories.name is '名称';
comment on column ems.usr_categories.org_id is '组织ID';
comment on column ems.usr_categories.remark is '备注';
comment on column ems.usr_categories.updated_at is '更新时间';
comment on column ems.usr_dimensions.domain_id is '业务系统ID';
comment on column ems.usr_dimensions.id is '非业务主键:auto_increment';
comment on column ems.usr_dimensions.key_name is '关键字';
comment on column ems.usr_dimensions.multiple is '是否多值';
comment on column ems.usr_dimensions.name is '名称';
comment on column ems.usr_dimensions.properties is '其他属性';
comment on column ems.usr_dimensions.required is '是否必须';
comment on column ems.usr_dimensions.source_ is '来源';
comment on column ems.usr_dimensions.title is '标题';
comment on column ems.usr_dimensions.value_type is '值类型';
comment on column ems.usr_group_members.group_id is '组ID';
comment on column ems.usr_group_members.id is '非业务主键:auto_increment';
comment on column ems.usr_group_members.is_granter is '可授权';
comment on column ems.usr_group_members.is_manager is '可管理';
comment on column ems.usr_group_members.is_member is '成员';
comment on column ems.usr_group_members.updated_at is '更新时间';
comment on column ems.usr_group_members.user_id is '用户ID';
comment on column ems.usr_groups.creator_id is '用户ID';
comment on column ems.usr_groups.enabled is '是否启用';
comment on column ems.usr_groups.id is '非业务主键:auto_increment';
comment on column ems.usr_groups.indexno is '顺序号';
comment on column ems.usr_groups.name is '名称';
comment on column ems.usr_groups.org_id is '组织ID';
comment on column ems.usr_groups.parent_id is '组ID';
comment on column ems.usr_groups.remark is '备注';
comment on column ems.usr_groups.updated_at is '更新时间';
comment on column ems.usr_groups_properties.dimension_id is '数据维度ID';
comment on column ems.usr_groups_properties.group_id is '组ID';
comment on column ems.usr_groups_properties.value_ is '组属性';
comment on column ems.usr_password_configs.dcredit is '密码中最少含有多少个数字';
comment on column ems.usr_password_configs.domain_id is '业务系统ID';
comment on column ems.usr_password_configs.id is '非业务主键:auto_increment';
comment on column ems.usr_password_configs.idledays is '密码停滞的天数';
comment on column ems.usr_password_configs.lcredit is '密码中最少含有多少个小写字母';
comment on column ems.usr_password_configs.maxdays is '密码保持有效的最大天数';
comment on column ems.usr_password_configs.maxlen is '密码的最大长度';
comment on column ems.usr_password_configs.minclass is '密码中最少含有几类字符';
comment on column ems.usr_password_configs.mindays is '密码可更改的最小天数';
comment on column ems.usr_password_configs.minlen is '密码的最小长度';
comment on column ems.usr_password_configs.ocredit is '密码中最少含有多少个其他字母';
comment on column ems.usr_password_configs.ucredit is '密码中最少含有多少个大写字母';
comment on column ems.usr_password_configs.usercheck is '是否检查密码中含有用户名';
comment on column ems.usr_password_configs.warnage is '用户密码到期前，提前收到警告信息的天数';
comment on column ems.usr_profiles.domain_id is '业务系统ID';
comment on column ems.usr_profiles.id is '非业务主键:auto_increment';
comment on column ems.usr_profiles.name is '名称';
comment on column ems.usr_profiles.user_id is '用户ID';
comment on column ems.usr_profiles_properties.dimension_id is '数据维度ID';
comment on column ems.usr_profiles_properties.profile_id is '用户配置ID';
comment on column ems.usr_profiles_properties.value_ is '用户配置-属性';
comment on column ems.usr_role_members.id is '非业务主键:auto_increment';
comment on column ems.usr_role_members.is_granter is '可授权';
comment on column ems.usr_role_members.is_manager is '可管理';
comment on column ems.usr_role_members.is_member is '成员';
comment on column ems.usr_role_members.role_id is '角色ID';
comment on column ems.usr_role_members.updated_at is '更新时间';
comment on column ems.usr_role_members.user_id is '用户ID';
comment on column ems.usr_roles.creator_id is '用户ID';
comment on column ems.usr_roles.domain_id is '业务系统ID';
comment on column ems.usr_roles.enabled is '是否启用';
comment on column ems.usr_roles.id is '非业务主键:auto_increment';
comment on column ems.usr_roles.indexno is '顺序号';
comment on column ems.usr_roles.name is '名称';
comment on column ems.usr_roles.parent_id is '角色ID';
comment on column ems.usr_roles.remark is '备注';
comment on column ems.usr_roles.updated_at is '更新时间';
comment on column ems.usr_roles_properties.dimension_id is '数据维度ID';
comment on column ems.usr_roles_properties.role_id is '角色ID';
comment on column ems.usr_roles_properties.value_ is '角色属性';
comment on column ems.usr_roots.app_id is '应用ID';
comment on column ems.usr_roots.id is '非业务主键:auto_increment';
comment on column ems.usr_roots.updated_at is '更新时间';
comment on column ems.usr_roots.user_id is '用户ID';
comment on column ems.usr_users.avatar_id is '头像ID';
comment on column ems.usr_users.begin_on is '生效日期';
comment on column ems.usr_users.category_id is '用户类别ID';
comment on column ems.usr_users.code is '帐号';
comment on column ems.usr_users.end_on is '失效日期';
comment on column ems.usr_users.id is '非业务主键:auto_increment';
comment on column ems.usr_users.name is '姓名';
comment on column ems.usr_users.org_id is '组织ID';
comment on column ems.usr_users.remark is '备注';
comment on column ems.usr_users.updated_at is '更新时间';
comment on table ems.blb_blob_metas is '文件信息';
comment on table ems.blb_profiles is '文件配置';
comment on table ems.cfg_app_groups is '应用分组';
comment on table ems.cfg_app_types is '应用类型';
comment on table ems.cfg_apps is '应用';
comment on table ems.cfg_credentials is '凭证';
comment on table ems.cfg_data_sources is '数据源';
comment on table ems.cfg_dbs is '数据库';
comment on table ems.cfg_dbs_properties is '数据源属性';
comment on table ems.cfg_domains is '业务系统';
comment on table ems.cfg_files is '项目文件';
comment on table ems.cfg_orgs is '组织';
comment on table ems.cfg_portalets is '页面小部件';
comment on table ems.cfg_portalets_categories is '面向用户';
comment on table ems.log_business_logs is '业务日志';
comment on table ems.log_levels is '日志级别';
comment on table ems.log_session_events is '会话日志';
comment on table ems.oa_docs is '文档';
comment on table ems.oa_docs_categories is '适用用户类别';
comment on table ems.oa_messages is '消息';
comment on table ems.oa_news is '新闻';
comment on table ems.oa_notices is '公告';
comment on table ems.oa_notices_categories is '适用用户类别';
comment on table ems.oa_notices_docs is '公告附件';
comment on table ems.oa_notifications is '通知';
comment on table ems.oa_sensitive_words is '敏感词汇';
comment on table ems.oa_todoes is '待办';
comment on table ems.se_app_permissions is '应用权限';
comment on table ems.se_data_permissions is '数据权限';
comment on table ems.se_data_resources is '数据资源';
comment on table ems.se_func_permissions is '功能权限';
comment on table ems.se_func_resources is '功能资源';
comment on table ems.se_menus is '菜单';
comment on table ems.se_menus_resources is '菜单关联资源';
comment on table ems.se_session_configs is '会话配置';
comment on table ems.usr_accounts is '账户';
comment on table ems.usr_avatars is '头像';
comment on table ems.usr_categories is '用户类别';
comment on table ems.usr_dimensions is '数据维度';
comment on table ems.usr_group_members is '组成员';
comment on table ems.usr_groups is '组';
comment on table ems.usr_groups_properties is '组属性';
comment on table ems.usr_password_configs is '密码配置';
comment on table ems.usr_profiles is '用户配置';
comment on table ems.usr_profiles_properties is '用户配置-属性';
comment on table ems.usr_role_members is '角色成员';
comment on table ems.usr_roles is '角色';
comment on table ems.usr_roles_properties is '角色属性';
comment on table ems.usr_roots is '根用户';
comment on table ems.usr_users is '用户';
