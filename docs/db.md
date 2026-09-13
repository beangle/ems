# 数据库 schema、建库脚本与迁移

## 权威来源

schema 的权威来源是 **实体类 + `beangle.xml` 里的 `<mapping>`/`<naming>` 配置**（`portal/src/main/resources/beangle.xml`），
`database.xml` 和各份 SQL 都是它的派生物：

| 文件 | 角色 |
|---|---|
| `portal/src/main/resources/db/postgresql/database.xml` | schema 快照（maven 风格的表/列/索引/注释描述），用于生成迁移脚本 |
| `portal/src/main/resources/db/postgresql/init/*.sql` | 新建库脚本，按 `0-schemas → 8-seed` 顺序执行 |
| `portal/src/main/resources/db/postgresql/migrate/*.sql` | 版本增量脚本，升级已有库时按版本号顺序执行 |
| `portal/src/main/resources/db/postgresql/report.xml` | DDL 报告配置（`sbt portal/ddlReport`） |

`init/8-seed.sql`（初始化数据）是手工维护的，生成器不会覆盖它。

> 2026-09-13 删除了 `db/postgresql/init/database.xml`：它是早期生成遗留的 schema 快照（51 张表，与 `database.xml` 的 73 张表已不一致），全仓库无引用。

## 重新生成 DDL / schema 快照

生成器只读代码与 `beangle.xml`，不连数据库：

```bash
sbt "portal/runMain org.beangle.data.orm.DdlGenerator PostgreSQL /tmp/ddlgen zh_CN"
# 产物：/tmp/ddlgen/postgresql/{0-schemas,1-tables,2-keys,3-indices,4-constraints,6-comments,7-auxiliaries}.sql
#       /tmp/ddlgen/postgresql/database.xml（快照）
#       /tmp/ddlgen/warnings.txt（生成告警，需逐条确认）
```

确认差异后再覆盖仓库里的文件（先 `diff` 看清楚，尤其注意删列/改类型）：

```bash
diff -u portal/src/main/resources/db/postgresql/init/1-tables.sql /tmp/ddlgen/postgresql/1-tables.sql
cp /tmp/ddlgen/postgresql/*.sql portal/src/main/resources/db/postgresql/init/
cp /tmp/ddlgen/postgresql/database.xml portal/src/main/resources/db/postgresql/database.xml
```

## 生成迁移脚本

`ddlDiff` 用两份历史快照做 diff，历史快照的命名必须是 `db/postgresql/db-<版本>.xml`：

```bash
# 先准备旧版本快照，例如旧版 database.xml 另存为 db-4.20.13.xml
cp portal/src/main/resources/db/postgresql/database.xml portal/src/main/resources/db/postgresql/db-4.20.13.xml
sbt "portal/ddlDiff 4.20.13 4.20.14"   # 产物：target/.../db/postgresql/migrate/4.20.13-4.20.14.sql
mv target/.../migrate/4.20.13-4.20.14.sql portal/src/main/resources/db/postgresql/migrate/
```

生成的是"结构差异"，涉及数据搬迁（改名、拆分、回填）时仍需手工补 SQL，并在发布说明里写清升级步骤。

## 已知问题（2026-09-13 校验）

用上面的生成命令与仓库里的文件逐一比对，发现快照和建库脚本**落后于实体类**，例如：

- `cfg_apps.base`、`cfg_apps.nav_style` 已从 `App` 移到 `Channel`（`Channel.base`、`Channel.embedMode`），但 `database.xml`、`init/*.sql` 里还是旧的；
- `Theme.gridHeaderBgColor` 未出现在快照和建库脚本中，颜色列长度也仍是 `varchar(15)`（实体类已是新的定义）。

下次发版前应按上面的步骤重新生成并逐条确认差异，同时补一份对应的 `migrate/*.sql`；在那之前，**以实体类为准**，不要直接照搬 `database.xml` 建库。
