# EMS 文档索引

本目录是 EMS 的设计与运维资料。按可信度分三类，改代码时请同步"现状"文档；"生成物"和"过程记录"不要手改。

## 现状（随代码维护）

| 文档 | 内容 | 需要同步的改动 |
|---|---|---|
| `db.md` | 数据库 schema 的权威来源、建库脚本生成、迁移生成 | 改实体/表结构、加迁移脚本 |
| `env.md` | 本地运行 Portal 的主类、参数与配置来源 | 改启动方式、主类、配置项 |
| `ems-cas.md`、`ems-cas-impl.md` | CAS 接入与实现（票据、密钥来源、配置项） | 改认证流程、票据、密钥 |
| `ems-app-permission.md` | 应用接入与权限模型（应用、渠道、菜单、角色环境） | 改权限/菜单/应用接入 |
| `system-url-list.md` | 主要 URL 清单（人工整理的关键入口，非全量） | 增删 Action 或映射后校对 |

## 生成物（不要手改）

| 文档 | 来源 | 说明 |
|---|---|---|
| `reflection-converged.md`、`reflection-converged.json` | native-image-agent 采集后与构建期 reflect-config 比对 | 2026-08-31 采集，仅对应当时代码 |
| `agent-reflect-gaps.md` | 同上，列出 JVM 上发生反射但 native 配置缺失的类 | 用于补注册反射，问题收敛后即失效 |

## 过程记录（历史，不再更新）

| 文档 | 说明 |
|---|---|
| `native-progress.md` | native-image 打通过程的流水账。构建/打包/发布的最终说明在 `beangle/build` 的 `docs/native.md` |

## 维护约定

- 新增文档先想清楚属于哪一类；过程记录写清"最后更新"日期，结论沉淀到现状文档。
- 现状文档里的命令、路径、类名要能直接跑通，改完顺手验证一遍。
