comment on column blb.blob_metas.domain_id is '业务系统ID';
comment on column blb.blob_metas.file_path is '文件路径';
comment on column blb.blob_metas.file_size is '大小';
comment on column blb.blob_metas.id is '非业务主键:datetime';
comment on column blb.blob_metas.media_type is '文件类型';
comment on column blb.blob_metas.name is '名称';
comment on column blb.blob_metas.owner is '所有者';
comment on column blb.blob_metas.profile_id is '文件配置ID';
comment on column blb.blob_metas.sha is 'Sha';
comment on column blb.blob_metas.updated_at is '更新时间';
comment on column blb.profiles.base is '基础路径';
comment on column blb.profiles.domain_id is '业务系统ID';
comment on column blb.profiles.id is '非业务主键:auto_increment';
comment on column blb.profiles.name is '名称';
comment on column blb.profiles.named_by_sha is '按照sha命名';
comment on column blb.profiles.public_download is '公开下载';
comment on column blb.profiles.users is '用户名';
comment on column oa.docs.app_id is '应用ID';
comment on column oa.docs.archived is '是否归档';
comment on column oa.docs.file_path is '路径';
comment on column oa.docs.file_size is '大小';
comment on column oa.docs.id is '非业务主键:datetime';
comment on column oa.docs.name is '名称';
comment on column oa.docs.updated_at is '更新时间';
comment on column oa.docs.upload_by_id is '上传人ID';
comment on column oa.docs_user_categories.doc_id is '文档ID';
comment on column oa.docs_user_categories.user_category_id is '用户类别ID';
comment on column oa.news.archived is '是否归档';
comment on column oa.news.contents is '内容';
comment on column oa.news.domain_id is '业务系统ID';
comment on column oa.news.id is '非业务主键:datetime';
comment on column oa.news.published_on is '发布日期';
comment on column oa.news.title is '标题';
comment on column oa.news.url is '链接地址';
comment on column oa.notices.app_id is '应用ID';
comment on column oa.notices.archived is '是否归档';
comment on column oa.notices.auditor_id is '审核人ID';
comment on column oa.notices.begin_on is '开始日期';
comment on column oa.notices.contents is '内容';
comment on column oa.notices.created_at is '创建时间';
comment on column oa.notices.end_on is '结束日期';
comment on column oa.notices.id is '非业务主键:datetime';
comment on column oa.notices.operator_id is '用户ID';
comment on column oa.notices.popup is '是否弹窗';
comment on column oa.notices.published_at is '发布日期';
comment on column oa.notices.status is '状态';
comment on column oa.notices.sticky is '是否置顶';
comment on column oa.notices.title is '标题';
comment on column oa.notices.updated_at is '更新时间';
comment on column oa.notices_docs.doc_id is '文档ID';
comment on column oa.notices_docs.notice_id is '公告ID';
comment on column oa.notices_user_categories.notice_id is '公告ID';
comment on column oa.notices_user_categories.user_category_id is '用户类别ID';
comment on column oa.sensitive_words.contents is '敏感词汇列表';
comment on column oa.sensitive_words.domain_id is '业务系统ID';
comment on column oa.sensitive_words.id is '非业务主键:auto_increment';
comment on column cfg.app_groups.domain_id is '业务系统ID';
comment on column cfg.app_groups.id is '非业务主键:auto_increment';
comment on column cfg.app_groups.indexno is '顺序号';
comment on column cfg.app_groups.name is '名称';
comment on column cfg.app_groups.title is '标题';
comment on column cfg.app_types.id is '非业务主键:auto_increment';
comment on column cfg.app_types.name is '名称';
comment on column cfg.app_types.title is '标题';
comment on column cfg.apps.app_type_id is '应用类型ID';
comment on column cfg.apps.base is '上下文';
comment on column cfg.apps.domain_id is '业务系统ID';
comment on column cfg.apps.enabled is '是否启用';
comment on column cfg.apps.group_id is '应用分组ID';
comment on column cfg.apps.id is '非业务主键:auto_increment';
comment on column cfg.apps.indexno is '排序索引号';
comment on column cfg.apps.logo_url is '应用LOGO';
comment on column cfg.apps.name is '应用名称';
comment on column cfg.apps.nav_style is '导航风格';
comment on column cfg.apps.remark is '备注';
comment on column cfg.apps.secret is '密钥';
comment on column cfg.apps.title is '标题';
comment on column cfg.apps.url is '应用地址';
comment on column cfg.credentials.domain_id is '业务系统ID';
comment on column cfg.credentials.expired_at is '过期时间';
comment on column cfg.credentials.id is '非业务主键:auto_increment';
comment on column cfg.credentials.name is '名称';
comment on column cfg.credentials.password is '密码';
comment on column cfg.credentials.updated_at is '更新时间';
comment on column cfg.credentials.username is '用户名';
comment on column cfg.data_sources.app_id is '应用ID';
comment on column cfg.data_sources.credential_id is '凭证ID';
comment on column cfg.data_sources.db_id is '数据库ID';
comment on column cfg.data_sources.id is '非业务主键:auto_increment';
comment on column cfg.data_sources.maximum_pool_size is '最大连接数';
comment on column cfg.data_sources.name is '名称';
comment on column cfg.data_sources.remark is '备注';
comment on column cfg.dbs.database_name is '数据库名';
comment on column cfg.dbs.domain_id is '业务系统ID';
comment on column cfg.dbs.driver is '驱动';
comment on column cfg.dbs.id is '非业务主键:auto_increment';
comment on column cfg.dbs.name is '名称';
comment on column cfg.dbs.port_number is '端口';
comment on column cfg.dbs.remark is '备注';
comment on column cfg.dbs.server_name is '服务器地址';
comment on column cfg.dbs.url is 'Java数据库连接字符串';
comment on column cfg.dbs_properties.db_id is '数据库ID';
comment on column cfg.dbs_properties.name is 'name';
comment on column cfg.dbs_properties.value_ is '数据源属性';
comment on column cfg.domains.hostname is '主机域名';
comment on column cfg.domains.id is '非业务主键:auto_increment';
comment on column cfg.domains.logo_url is 'logo 网址';
comment on column cfg.domains.name is '名称';
comment on column cfg.domains.org_id is '组织ID';
comment on column cfg.domains.title is '标题';
comment on column cfg.files.app_id is '应用ID';
comment on column cfg.files.file_path is '路径';
comment on column cfg.files.file_size is '文件大小';
comment on column cfg.files.id is '非业务主键:auto_increment';
comment on column cfg.files.media_type is '类型';
comment on column cfg.files.name is '名称';
comment on column cfg.files.updated_at is '更新时间';
comment on column cfg.orgs.code is '代码';
comment on column cfg.orgs.id is '非业务主键:auto_increment';
comment on column cfg.orgs.logo_url is '组织Logo';
comment on column cfg.orgs.name is '名称';
comment on column cfg.orgs.remark is '备注';
comment on column cfg.orgs.short_name is '简称';
comment on column cfg.orgs.www_url is '网站地址';
comment on column log.business_logs.agent is '操作客户端代理';
comment on column log.business_logs.app_id is '应用ID';
comment on column log.business_logs.details is '操作内容';
comment on column log.business_logs.entry is '访问入口';
comment on column log.business_logs.id is '非业务主键:datetime';
comment on column log.business_logs.ip is 'IP';
comment on column log.business_logs.level_id is '日志级别ID';
comment on column log.business_logs.operate_at is '操作时间';
comment on column log.business_logs.operator is '操作人';
comment on column log.business_logs.resources is '对应的资源';
comment on column log.business_logs.summary is '操作内容摘要';
comment on column log.levels.id is '非业务主键:auto_increment';
comment on column log.levels.name is '名称';
comment on column log.session_events.detail is '明细';
comment on column log.session_events.domain_id is '业务系统ID';
comment on column log.session_events.event_type is '时间类型';
comment on column log.session_events.id is '非业务主键:datetime';
comment on column log.session_events.ip is 'IP';
comment on column log.session_events.name is '名称';
comment on column log.session_events.principal is '用户名';
comment on column log.session_events.updated_at is '更新时间';
comment on column log.session_events.username is '账户';
comment on column se.app_permissions.actions is '操作';
comment on column se.app_permissions.app_id is '应用ID';
comment on column se.app_permissions.begin_at is '生效时间';
comment on column se.app_permissions.end_at is '失效时间';
comment on column se.app_permissions.id is '非业务主键:auto_increment';
comment on column se.app_permissions.resource_id is '功能资源ID';
comment on column se.app_permissions.restrictions is '限制条件';
comment on column se.data_permissions.actions is '操作';
comment on column se.data_permissions.app_id is '应用ID';
comment on column se.data_permissions.attrs is '属性';
comment on column se.data_permissions.begin_at is '开始时间';
comment on column se.data_permissions.description is '描述';
comment on column se.data_permissions.domain_id is '业务系统ID';
comment on column se.data_permissions.end_at is '结束时间';
comment on column se.data_permissions.filters is '过滤条件';
comment on column se.data_permissions.func_resource_id is '功能资源ID';
comment on column se.data_permissions.id is '非业务主键:auto_increment';
comment on column se.data_permissions.remark is '备注';
comment on column se.data_permissions.resource_id is '数据资源ID';
comment on column se.data_permissions.restrictions is '限制条件';
comment on column se.data_permissions.role_id is '角色ID';
comment on column se.data_resources.actions is '操作';
comment on column se.data_resources.domain_id is '业务系统ID';
comment on column se.data_resources.id is '非业务主键:auto_increment';
comment on column se.data_resources.name is '名称';
comment on column se.data_resources.remark is '备注';
comment on column se.data_resources.scope_ is '范围';
comment on column se.data_resources.title is '标题';
comment on column se.data_resources.type_name is '类型';
comment on column se.func_permissions.actions is '操作';
comment on column se.func_permissions.begin_at is '生效时间';
comment on column se.func_permissions.end_at is '失效时间';
comment on column se.func_permissions.id is '非业务主键:auto_increment';
comment on column se.func_permissions.remark is '备注';
comment on column se.func_permissions.resource_id is '功能资源ID';
comment on column se.func_permissions.restrictions is '限制条件';
comment on column se.func_permissions.role_id is '角色ID';
comment on column se.func_resources.actions is '操作';
comment on column se.func_resources.app_id is '应用ID';
comment on column se.func_resources.enabled is '是否启用';
comment on column se.func_resources.id is '非业务主键:auto_increment';
comment on column se.func_resources.name is '名称';
comment on column se.func_resources.remark is '备注';
comment on column se.func_resources.scope_ is '范围';
comment on column se.func_resources.title is '标题';
comment on column se.menus.app_id is '应用ID';
comment on column se.menus.enabled is '是否启用';
comment on column se.menus.entry_id is '功能资源ID';
comment on column se.menus.id is '非业务主键:auto_increment';
comment on column se.menus.indexno is '顺序号';
comment on column se.menus.name is '名称';
comment on column se.menus.params is '参数';
comment on column se.menus.parent_id is '菜单ID';
comment on column se.menus.remark is '备注';
comment on column se.menus.title is '标题';
comment on column se.menus_resources.func_resource_id is '功能资源ID';
comment on column se.menus_resources.menu_id is '菜单ID';
comment on column ssn.session_configs.capacity is '最大容量';
comment on column ssn.session_configs.category_id is '用户类别ID';
comment on column ssn.session_configs.check_capacity is '是否检查容量';
comment on column ssn.session_configs.check_concurrent is '是否检查多重会话';
comment on column ssn.session_configs.concurrent is '多重会话数';
comment on column ssn.session_configs.domain_id is '业务系统ID';
comment on column ssn.session_configs.id is '非业务主键:datetime';
comment on column ssn.session_configs.tti_minutes is '过期时间';
comment on column usr.accounts.begin_on is '生效日期';
comment on column usr.accounts.domain_id is '业务系统ID';
comment on column usr.accounts.enabled is '是否启用';
comment on column usr.accounts.end_on is '失效日期';
comment on column usr.accounts.id is '非业务主键:auto_increment';
comment on column usr.accounts.locked is '是否锁定';
comment on column usr.accounts.passwd_expired_on is '密码过期日期';
comment on column usr.accounts.password is '密码';
comment on column usr.accounts.updated_at is '更新时间';
comment on column usr.accounts.user_id is '用户ID';
comment on column usr.avatars.file_name is '文件名';
comment on column usr.avatars.file_path is '文件路径';
comment on column usr.avatars.id is '非业务主键:assigned';
comment on column usr.avatars.updated_at is '更新时间';
comment on column usr.avatars.user_id is '用户ID';
comment on column usr.dimensions.domain_id is '业务系统ID';
comment on column usr.dimensions.id is '非业务主键:auto_increment';
comment on column usr.dimensions.key_name is '关键字';
comment on column usr.dimensions.multiple is '是否多值';
comment on column usr.dimensions.name is '名称';
comment on column usr.dimensions.properties is '其他属性';
comment on column usr.dimensions.required is '是否必须';
comment on column usr.dimensions.source_ is '来源';
comment on column usr.dimensions.title is '标题';
comment on column usr.dimensions.value_type is '值类型';
comment on column usr.group_members.group_id is '组ID';
comment on column usr.group_members.id is '非业务主键:auto_increment';
comment on column usr.group_members.is_granter is '可授权';
comment on column usr.group_members.is_manager is '可管理';
comment on column usr.group_members.is_member is '成员';
comment on column usr.group_members.updated_at is '更新时间';
comment on column usr.group_members.user_id is '用户ID';
comment on column usr.messages.contents is '内容';
comment on column usr.messages.id is '非业务主键:auto_increment';
comment on column usr.messages.recipient_id is '接受人ID';
comment on column usr.messages.sender_id is '发送人ID';
comment on column usr.messages.sent_at is '发送时间';
comment on column usr.messages.status is '状态';
comment on column usr.messages.title is '标题';
comment on column usr.notifications.contents is '内容';
comment on column usr.notifications.id is '非业务主键:auto_increment';
comment on column usr.notifications.importance is '重要程度';
comment on column usr.notifications.recipient_id is '接受人ID';
comment on column usr.notifications.sent_at is '发送时间';
comment on column usr.notifications.subject is '主题';
comment on column usr.password_configs.dcredit is '密码中最少含有多少个数字';
comment on column usr.password_configs.domain_id is '业务系统ID';
comment on column usr.password_configs.id is '非业务主键:auto_increment';
comment on column usr.password_configs.idledays is '密码停滞的天数';
comment on column usr.password_configs.lcredit is '密码中最少含有多少个小写字母';
comment on column usr.password_configs.maxdays is '密码保持有效的最大天数';
comment on column usr.password_configs.maxlen is '密码的最大长度';
comment on column usr.password_configs.minclass is '密码中最少含有几类字符';
comment on column usr.password_configs.mindays is '密码可更改的最小天数';
comment on column usr.password_configs.minlen is '密码的最小长度';
comment on column usr.password_configs.ocredit is '密码中最少含有多少个其他字母';
comment on column usr.password_configs.ucredit is '密码中最少含有多少个大写字母';
comment on column usr.password_configs.usercheck is '是否检查密码中含有用户名';
comment on column usr.password_configs.warnage is '用户密码到期前，提前收到警告信息的天数';
comment on column usr.role_members.id is '非业务主键:auto_increment';
comment on column usr.role_members.is_granter is '可授权';
comment on column usr.role_members.is_manager is '可管理';
comment on column usr.role_members.is_member is '成员';
comment on column usr.role_members.role_id is '角色ID';
comment on column usr.role_members.updated_at is '更新时间';
comment on column usr.role_members.user_id is '用户ID';
comment on column usr.roles.creator_id is '用户ID';
comment on column usr.roles.domain_id is '业务系统ID';
comment on column usr.roles.enabled is '是否启用';
comment on column usr.roles.id is '非业务主键:auto_increment';
comment on column usr.roles.indexno is '顺序号';
comment on column usr.roles.name is '名称';
comment on column usr.roles.parent_id is '角色ID';
comment on column usr.roles.remark is '备注';
comment on column usr.roles.updated_at is '更新时间';
comment on column usr.roles_properties.dimension_id is '数据维度ID';
comment on column usr.roles_properties.role_id is '角色ID';
comment on column usr.roles_properties.value_ is '角色属性';
comment on column usr.roots.app_id is '应用ID';
comment on column usr.roots.id is '非业务主键:auto_increment';
comment on column usr.roots.updated_at is '更新时间';
comment on column usr.roots.user_id is '用户ID';
comment on column usr.todoes.contents is '内容';
comment on column usr.todoes.domain_id is '业务系统ID';
comment on column usr.todoes.id is '非业务主键:auto_increment';
comment on column usr.todoes.updated_at is '更新时间';
comment on column usr.todoes.user_id is '用户ID';
comment on column usr.user_categories.begin_on is '生效日期';
comment on column usr.user_categories.code is '代码';
comment on column usr.user_categories.en_name is '英文名';
comment on column usr.user_categories.end_on is '失效日期';
comment on column usr.user_categories.id is '非业务主键:auto_increment';
comment on column usr.user_categories.name is '名称';
comment on column usr.user_categories.org_id is '组织ID';
comment on column usr.user_categories.remark is '备注';
comment on column usr.user_categories.updated_at is '更新时间';
comment on column usr.user_groups.creator_id is '用户ID';
comment on column usr.user_groups.enabled is '是否启用';
comment on column usr.user_groups.id is '非业务主键:auto_increment';
comment on column usr.user_groups.indexno is '顺序号';
comment on column usr.user_groups.name is '名称';
comment on column usr.user_groups.org_id is '组织ID';
comment on column usr.user_groups.parent_id is '组ID';
comment on column usr.user_groups.remark is '备注';
comment on column usr.user_groups.updated_at is '更新时间';
comment on column usr.user_groups_properties.dimension_id is '数据维度ID';
comment on column usr.user_groups_properties.user_group_id is '组ID';
comment on column usr.user_groups_properties.value_ is '组属性';
comment on column usr.user_profiles.domain_id is '业务系统ID';
comment on column usr.user_profiles.id is '非业务主键:auto_increment';
comment on column usr.user_profiles.name is '名称';
comment on column usr.user_profiles.user_id is '用户ID';
comment on column usr.user_profiles_properties.dimension_id is '数据维度ID';
comment on column usr.user_profiles_properties.user_profile_id is '用户配置ID';
comment on column usr.user_profiles_properties.value_ is '用户配置-属性';
comment on column usr.users.avatar_id is '头像ID';
comment on column usr.users.begin_on is '生效日期';
comment on column usr.users.category_id is '用户类别ID';
comment on column usr.users.code is '帐号';
comment on column usr.users.end_on is '失效日期';
comment on column usr.users.id is '非业务主键:auto_increment';
comment on column usr.users.name is '姓名';
comment on column usr.users.org_id is '组织ID';
comment on column usr.users.remark is '备注';
comment on column usr.users.updated_at is '更新时间';
comment on column usr.users_properties.dimension_id is '数据维度ID';
comment on column usr.users_properties.user_id is '用户ID';
comment on column usr.users_properties.value_ is '属性';
comment on table blb.blob_metas is '文件信息';
comment on table blb.profiles is '文件配置';
comment on table oa.docs is '文档';
comment on table oa.docs_user_categories is '适用用户类别';
comment on table oa.news is '新闻';
comment on table oa.notices is '公告';
comment on table oa.notices_docs is '公告附件';
comment on table oa.notices_user_categories is '适用用户类别';
comment on table oa.sensitive_words is '敏感词汇';
comment on table cfg.app_groups is '应用分组';
comment on table cfg.app_types is '应用类型';
comment on table cfg.apps is '应用';
comment on table cfg.credentials is '凭证';
comment on table cfg.data_sources is '数据源';
comment on table cfg.dbs is '数据库';
comment on table cfg.dbs_properties is '数据源属性';
comment on table cfg.domains is '业务系统';
comment on table cfg.files is '项目文件';
comment on table cfg.orgs is '组织';
comment on table log.business_logs is '业务日志';
comment on table log.levels is '日志级别';
comment on table log.session_events is '会话日志';
comment on table se.app_permissions is '应用权限';
comment on table se.data_permissions is '数据权限';
comment on table se.data_resources is '数据资源';
comment on table se.func_permissions is '功能权限';
comment on table se.func_resources is '功能资源';
comment on table se.menus is '菜单';
comment on table se.menus_resources is '菜单关联资源';
comment on table ssn.session_configs is '会话配置';
comment on table usr.accounts is '账户';
comment on table usr.avatars is '头像';
comment on table usr.dimensions is '数据维度';
comment on table usr.group_members is '组成员';
comment on table usr.messages is '消息';
comment on table usr.notifications is '通知';
comment on table usr.password_configs is '密码配置';
comment on table usr.role_members is '角色成员';
comment on table usr.roles is '角色';
comment on table usr.roles_properties is '角色属性';
comment on table usr.roots is '根用户';
comment on table usr.todoes is '待办';
comment on table usr.user_categories is '用户类别';
comment on table usr.user_groups is '组';
comment on table usr.user_groups_properties is '组属性';
comment on table usr.user_profiles is '用户配置';
comment on table usr.user_profiles_properties is '用户配置-属性';
comment on table usr.users is '用户';
comment on table usr.users_properties is '属性';
