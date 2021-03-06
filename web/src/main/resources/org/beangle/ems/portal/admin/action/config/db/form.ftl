[#ftl]
[@b.head/]
[@b.toolbar title="新建/修改数据源"]bar.addBack();[/@]
<style>form.listform label.title{width:120px;}</style>
[@b.tabs]
  [#assign save_action][#if db.persisted]!update?id=${db.id}[#else]!save[/#if][/#assign]
  [@b.form action=save_action theme="list"]
    [@b.textfield name="db.name" label="名称" value="${db.name!}" required="true" maxlength="200"/]
    [@b.select items=drivers name="db.driver" label="驱动" value="${db.driver!}" required="true" maxlength="200"/]
    [@b.textfield name="db.serverName" label="IP或者服务器名" value="${db.serverName!}" required="true" maxlength="200"/]
    [@b.textfield name="db.databaseName" label="数据库名" value="${db.databaseName!}" required="true" maxlength="200"/]
    [@b.textfield name="db.portNumber" label="数据库端口" value="${db.portNumber!}" required="true" maxlength="200"/]
    [@b.textfield name="db.url" label="URL" value="${db.url!}" required="false" maxlength="200" style="width:300px;"/]
    [@b.textarea name="properties" label="其他属性" value="${db.propertiesString}" maxlength="400" cols="50" comment="<a href='https://github.com/brettwooldridge/HikariCP' target='_blank'>属性参考</a>"/]
    [@b.textarea name="db.remark" label="备注" value="${db.remark!}" maxlength="200"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]
