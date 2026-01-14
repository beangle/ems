[#ftl]
[@b.head/]

[@b.grid items=apps var="app"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="10%" property="code" title="代码"/]
    [@b.col property="name" title="名称"/]
    [@b.col width="30%" property="secret" title="密钥"]
      <span title="${app.secret}">${(app.secret[0..2])!}****${(app.secret[7..10])!}</span>
    [/@]
    [@b.col width="15%" property="updatedAt" title="更新日期"/]
  [/@]
[/@]
[@b.foot/]
