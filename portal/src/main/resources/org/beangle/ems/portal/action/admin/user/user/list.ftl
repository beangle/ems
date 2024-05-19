[#ftl]
[@b.head/]
[#include "../status.ftl"/]
[@b.grid items=users var="user"]
  [@b.gridbar]
  function activateUser(isActivate){return action.multi("activate","确定提交?","isActivate="+isActivate);}
  bar.addItem("${b.text("action.new")}",action.add());
  bar.addItem("${b.text("action.modify")}",action.edit());
  bar.addItem("${b.text("action.freeze")}",activateUser('false'),'action-freeze');
  bar.addItem("${b.text("action.activate")}",activateUser('true'),'action-activate');
  bar.addItem("${b.text("action.delete")}",action.remove());
  bar.addItem("${b.text("action.export")}",action.exportData("user.code:账户,user.name:姓名,user.category.name:身份,user.mobile:手机,user.email:电子邮箱,beginOn:创建日期,endOn:结束日期,roleNames:角色,enabled:是否启用,locked:是否锁定","Xlsx","&fileName=用户信息"));
 [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="code" width="12%"]${user.code}[/@]
    [@b.col property="name" width="10%"][@b.a href="!dashboard?id="+user.id target="_blank"]${user.name}[/@][/@]
    [@b.col property="category.name" width="8%"/]
    [@b.col property="group.name" title="默认用户组" width="10%"/]
    [@b.col title="角色"]
      [#assign members=[]]
      [#list user.roles?sort_by(['role','indexno']) as m][#if m.role.domain=domain] [#assign members=members+[m]][/#if][/#list]
      [#list members?sort_by(['role','indexno']) as m][#if m.member]${m.role.name}&nbsp;[/#if][/#list]
    [/@]
    [@b.col property="mobile" title="手机号码" width="9%"]
      [#if user.mobile?? && user.mobile?length>10]
        <span title="${user.mobile}">${(user.mobile[0..2])!}****${(user.mobile[7..10])!}</span>
      [#else]
        ${(user.mobile)!"--"}
      [/#if]
    [/@]
    [@b.col property="beginOn" width="9%"]${user.beginOn}~${(user.endOn)!}[/@]
    [@b.col property="passwdExpiredOn" width="9%"]${(user.passwdExpiredOn)!}[/@]
    [@b.col property="enabled" width="6%"][@enableInfo user.enabled/][/@]
  [/@]
[/@]
[@b.foot/]
