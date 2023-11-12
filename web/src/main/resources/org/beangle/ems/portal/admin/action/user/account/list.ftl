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
    [@b.col property="user.code" width="12%"]${account.user.code}[/@]
    [@b.col property="user.name" width="15%"/]
    [@b.col property="user.category.name" width="12%"/]
    [@b.col title="角色"]
      [#assign members=[]]
      [#list account.user.roles?sort_by(['role','indexno']) as m][#if m.role.domain=domain] [#assign members=members+[m]][/#if][/#list]
      [#list members?sort_by(['role','indexno']) as m][#if m.member]${m.role.name}&nbsp;[/#if][/#list]
    [/@]
    [@b.col property="user.mobile" title="手机号码" width="9%"]
      [#if account.user.mobile?? && account.user.mobile?length>10]
        <span title="${account.user.mobile}">${(account.user.mobile[0..2])!}****${(account.user.mobile[7..10])!}</span>
      [#else]
        ${(account.user.mobile)!"--"}
      [/#if]
    [/@]
    [@b.col property="beginOn" width="12%"]${account.beginOn}~${(account.endOn)!}[/@]
    [@b.col property="passwdExpiredOn" width="10%"]${(account.passwdExpiredOn)!}[/@]
    [@b.col property="enabled" width="8%" ][@enableInfo account.enabled/][/@]
  [/@]
[/@]
[@b.foot/]
