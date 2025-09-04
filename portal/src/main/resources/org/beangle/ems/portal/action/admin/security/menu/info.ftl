[#ftl]
[@b.head/]
[@b.toolbar title="info.module.detail"]
bar.addBack("${b.text("action.back")}");
[/@]
<table class="table table-sm table-detail">
 <tr>
   <td class="title" width="15%">&nbsp;${b.text("common.id")}:</td>
   <td >${menu.indexno}  </td>
   <td class="title"  width="15%">&nbsp;${b.text("menu.entry")}:</td>
   <td >
   [#if menu.entry??]${menu.entry.name}[/#if][#if menu.params??]?${menu.params?html}[/#if]
   </td>
 </tr>
 <tr>
   <td class="title" >&nbsp;${b.text("common.name")}:</td>
   <td >${menu.name!}</td>
   <td class="title" >&nbsp;英文名:</td>
   <td >${menu.enName}</td>
 </tr>
 <tr>
  <td class="title" >&nbsp;${b.text("common.remark")}:</td>
  <td   >${menu.remark!}</td>
  <td class="title" >&nbsp;${b.text("common.status")}:</td>
  <td >
    [#if menu.enabled]${b.text("action.activate")}[#else]${b.text("action.unactivate")}[/#if]
  </td>
 </tr>
 <tr>
   <td class="title" >&nbsp;引用资源:</td>
   <td >[#list menu.resources as resource](${resource.name})${resource.title}<br/>[/#list]</td>
   <td class="title">角色:</td>
   <td>
    [#list roles! as role]${role.name}[#if role_has_next]<br/>[/#if][/#list]
   </td>
 </tr>
</table>
[@b.foot/]
