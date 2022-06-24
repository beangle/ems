alter table blb.blob_metas add constraint pk_712d2we8hkudp42k529irbs85 primary key (id);
alter table blb.profiles add constraint pk_paulq8mh13e9yu5bvo8g6kgft primary key (id);
alter table bulletin.docs add constraint pk_qif6gd1jn4vc7dj4oanl8dpur primary key (id);
alter table bulletin.docs_user_categories add constraint pk_9dnkujcthlvg7fnq6mjwr2lv1 primary key (doc_id,user_category_id);
alter table bulletin.news add constraint pk_9d5rnbtss6v14glhivuu8nnxh primary key (id);
alter table bulletin.notices add constraint pk_5002frr9faurpclvlj4rchxpk primary key (id);
alter table bulletin.notices_docs add constraint pk_ppflm7s14y4f48ptl6ix8oqlm primary key (notice_id,doc_id);
alter table bulletin.notices_user_categories add constraint pk_ji4mbg1919dby7ovm36ch9py7 primary key (notice_id,user_category_id);
alter table bulletin.sensitive_words add constraint pk_93tcwxw8v9r64kdlt5ljsawak primary key (id);
alter table cfg.app_groups add constraint idx_app_group unique (domain_id,name);
alter table cfg.app_groups add constraint pk_kya1tu2eyjl7ubus7ik809f1o primary key (id);
alter table cfg.app_types add constraint pk_8tlkrjx3okd543sof65hv26ww primary key (id);
alter table cfg.app_types add constraint uk_fiof2g9fn0dk5dghxnctssvf0 unique (name);
alter table cfg.apps add constraint idx_app unique (domain_id,name);
alter table cfg.apps add constraint pk_hy5ovfd6iqkwwmeqkoyovyc0f primary key (id);
alter table cfg.credentials add constraint idx_credential unique (domain_id,name);
alter table cfg.credentials add constraint pk_onen3x0wxu60l38jaqwd1230k primary key (id);
alter table cfg.data_sources add constraint idx_datasource unique (app_id,name);
alter table cfg.data_sources add constraint pk_86ttadu3n7pn6c82rdtxh1d1m primary key (id);
alter table cfg.dbs add constraint idx_db unique (domain_id,name);
alter table cfg.dbs add constraint pk_fjmvnbma8ydciv9wa0rocjmw1 primary key (id);
alter table cfg.dbs_properties add constraint pk_bt0xt6wvgav0aqxwvw5hurxk2 primary key (db_id,value_,name);
alter table cfg.domains add constraint idx_domain unique (org_id,hostname);
alter table cfg.domains add constraint pk_qmiqopkrlr3yjian3tuafihea primary key (id);
alter table cfg.files add constraint pk_8429obi968vjdc8arvkoqgqvw primary key (id);
alter table cfg.orgs add constraint pk_a9p5unhabl77t6wiof0meuwok primary key (id);
alter table log.business_logs add constraint pk_e7nfc0kcwyw2dvnpa3ik7dryx primary key (id);
alter table log.levels add constraint pk_fagiox162rmboh86j3wvglkic primary key (id);
alter table log.session_events add constraint pk_let8o6waxxtw4p6ae3tvfbgij primary key (id);
alter table se.app_permissions add constraint pk_gak8t8oul1xrpq4t02593ihhk primary key (id);
alter table se.data_permissions add constraint pk_8bdqy5re8q4ab6beh9gxm4qlg primary key (id);
alter table se.data_resources add constraint pk_ccp9osc7grc5vtqdjdfr47f4w primary key (id);
alter table se.func_permissions add constraint pk_h94wbison19lr3y7yppgb5ak2 primary key (id);
alter table se.func_resources add constraint pk_4jufsum1ynbseshirn6fpwlw1 primary key (id);
alter table se.menus add constraint pk_f3r7oef25midhmnnur5fthqah primary key (id);
alter table se.menus_resources add constraint pk_e7bj714s46w1fks68ay6vrhc9 primary key (menu_id,func_resource_id);
alter table ssn.session_configs add constraint idx_session_config unique (domain_id,category_id);
alter table ssn.session_configs add constraint pk_qd06y5urtnkq9yc35lfb79s7c primary key (id);
alter table usr.accounts add constraint idx_account unique (user_id,domain_id);
alter table usr.accounts add constraint pk_e9c7rnsff82leuiecb1tbk45a primary key (id);
alter table usr.avatars add constraint pk_kc3w67oufvtlendg1b4uvayqb primary key (id);
alter table usr.dimensions add constraint idx_dimension_name unique (domain_id,name);
alter table usr.dimensions add constraint pk_sedg73ys3wkrqexgwrpb932cp primary key (id);
alter table usr.group_members add constraint pk_1walq9hajwl1vn0km8pe1r967 primary key (id);
alter table usr.messages add constraint pk_pv8na0rpo5rbptb62nf4s4v8s primary key (id);
alter table usr.notifications add constraint pk_sbyeg0j0meutlfvf5squ2oma3 primary key (id);
alter table usr.password_configs add constraint idx_password_config unique (domain_id);
alter table usr.password_configs add constraint pk_ktglwm779su3bvsmvdgpueox8 primary key (id);
alter table usr.role_members add constraint pk_noisvuh3ixbcth7ln6r1cc9kc primary key (id);
alter table usr.roles add constraint idx_role_name unique (domain_id,name);
alter table usr.roles add constraint pk_7q9pjp2665ovxfk0i1bu7tsgs primary key (id);
alter table usr.roles_properties add constraint pk_fqotio4169qkyqlanurbkca5v primary key (role_id,value_,dimension_id);
alter table usr.roots add constraint pk_n1in0fo3cbi67rpcxyyopc54r primary key (id);
alter table usr.todoes add constraint pk_rsgwsrqg8o0qbyrac9hoe3ohb primary key (id);
alter table usr.user_categories add constraint idx_user_category unique (org_id,name);
alter table usr.user_categories add constraint pk_g22tbnb6clymkxmi560ll33em primary key (id);
alter table usr.user_groups add constraint idx_group unique (org_id,name);
alter table usr.user_groups add constraint pk_it5g3etsarg9hkfu7e664rw1b primary key (id);
alter table usr.user_groups_properties add constraint pk_3otcvvifvwfy86b0x6tp32qcu primary key (user_group_id,value_,dimension_id);
alter table usr.user_profiles add constraint pk_kcn0fy938doidu2npqnmi1jsx primary key (id);
alter table usr.user_profiles_properties add constraint pk_c456k2swg32okt7ycc69rqhko primary key (user_profile_id,value_,dimension_id);
alter table usr.users add constraint idx_user_code unique (org_id,code);
alter table usr.users add constraint pk_i4mlr3lp5ixml05sx2wuk8kp8 primary key (id);
alter table usr.users_properties add constraint pk_8qv4lprfwfg6vcxqxeusnv7j3 primary key (user_id,value_,dimension_id);
