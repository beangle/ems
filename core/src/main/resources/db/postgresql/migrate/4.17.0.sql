alter table ems.cfg_third_party_apps add redirect_uri varchar(500);
create table ems.se_oauth_tokens (expired_at timestamptz not null, client_id bigint not null, token varchar(500) not null, user_id bigint not null, issued_at timestamptz not null, id bigint not null, scope_ varchar(200) not null);

alter table ems.se_oauth_tokens add constraint pk_c60mjym9bnp183t93i99omr2x primary key (id);
