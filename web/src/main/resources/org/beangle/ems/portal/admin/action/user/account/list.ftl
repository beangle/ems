[#ftl]
[@b.head/]
[#include "../status.ftl"/]
[@b.grid items=accounts var="account"]
  [@b.gridbar]
  function activateUser(isActivate){return action.multi("activate","确定提交?","isActivate="+isActivate);}
  bar.addItem("${b.text("action.new")}",action.add());
  bar.addItem("${b.text("action.modify")}",action.edit());
  bar.addItem("${b.text("action.freeze")}",activateUser('false'),'action-freeze');
  bar.addItem("${b.text("action.activate")}",activateUser('true'),'action-activate');
  bar.addItem("${b.text("action.delete")}",action.remove());
  bar.addItem("${b.text("action.export")}",action.exportData("user.code:账户,user.name:姓名,user.category.name:身份,beginOn:创建日期,endOn:结束日期,roleNames:角色,enabled:是否启用,locked:是否锁定","Xlsx","&fileName=用户信息"));
 [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="user.code" title="账户" width="15%"]${account.user.code}[/@]
    [@b.col property="user.name" title="姓名" width="20%"/]
    [@b.col property="user.category.name" title="身份" width="12%"/]
    [@b.col title="角色" width="27%"]
      [#assign members=[]]
      [#list account.user.roles?sort_by(['role','indexno']) as m][#if m.role.domain=domain] [#assign members=members+[m]][/#if][/#list]
      [#list members?sort_by(['role','indexno']) as m][#if m.member]${m.role.name}&nbsp;[/#if][/#list]
    [/@]
    [@b.col property="beginOn" title="有效期" width="20%"]${account.beginOn}~${(account.endOn)!}[/@]
    [@b.col property="enabled" width="8%" title="是否可用"][@enableInfo account.enabled/][/@]
  [/@]
[/@]
[@b.foot/]
