[#ftl]
[@b.head/]
[@b.toolbar title='info.role']
bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
   <td class="title" width="10%">${b.text("common.name")}:</td>
   <td class="content" width="40%"> ${role.name}</td>
   <td class="title" width="10%">${b.text("common.creator")}:</td>
   <td class="content" width="40%">${(role.creator.name)!}  </td>
  </tr>
  <tr>
   <td class="title" >修改时间:</td>
   <td class="content">${role.updatedAt}</td>
  </tr>
  <tr>
  <td class="title" >&nbsp;${b.text("common.status")}:</td>
  <td class="content" colspan="3">
    [#if role.enabled] ${b.text("action.activate")}
    [#else]${b.text("action.freeze")}
    [/#if]
  </td>
  </tr>
  <tr>
  <td class="title" >${b.text("common.remark")}:</td>
  <td  class="content" colspan="3">${role.remark!}</td>
  </tr>
  [#--<tr>
    <td colspan="4">[@b.div href="restriction!info?restriction.holder.id=${role.id}&restrictionType=role" /]</td>
  </tr>
  --]
</table>
[@b.foot/]
