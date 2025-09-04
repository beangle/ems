[#ftl]
[@b.head/]
[#include "scope.ftl"/]
[@b.toolbar title="app.funcresource.info"]bar.addBack();[/@]
<table class="table table-sm table-detail">
   <tr>
     <td class="title" width="15%">标题:</td>
     <td  width="35%">${resource.title}</td>
     <td class="title" width="15%">${b.text("common.name")}:</td>
     <td  width="35%">${resource.name}</td>
   </tr>
   <tr>
     <td class="title">&nbsp;可见范围:</td>
     <td >[@resourceScope resource.scope/]</td>
     <td class="title">&nbsp;${b.text("common.status")}:</td>
     <td >
    [#if resource.enabled]${b.text("action.activate")}[#else]${b.text("action.unactivate")}[/#if]
     </td>
   </tr>
   <tr>
     <td class="title">引用菜单:</td>
     <td >[#list menus as menu](${menu.indexno})${menu.name}<br/>[/#list]</td>
     <td class="title">${b.text("common.remark")}:</td>
     <td >${resource.remark!}</td>
   </tr>
   <tr>
     <td class="title">角色:</td>
     <td  colspan="3">
    [#list roles as role]${role.name}[#if role_has_next]&nbsp;[/#if][/#list]
     </td>
  </tr>
</table>
[@b.foot/]
