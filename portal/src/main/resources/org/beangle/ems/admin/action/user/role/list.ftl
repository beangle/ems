[#ftl]
[@b.head/]
<script type="text/javascript">
  beangle.load(["bui-tabletree"]);
</script>
[#include "../status.ftl"/]
[@b.grid  items=roles var="role" sortable="false"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("删除时，会级联删除对应的所有子角色，确认删除?"));
    bar.addItem("${b.text("action.export")}",action.exportData("name:${b.text("common.name")},remark:${b.text("common.remark")},creator.name:${b.text("common.creator")},createdAt:${b.text("common.createdAt")},updatedAt:${b.text("common.updatedAt")},users:${b.text("role.users")}",null,"&fileName=角色"));
  [/@]
  [@b.row]
    <tr id="${(role.indexno)!}">
    [@b.boxcol /]
    [@b.treecol property="name" width="36%" title="common.name"][@b.a href="!info?id=${role.id}"]${role.indexno} ${role.name}[/@]
    [/@]
    [@b.col width="18%" property="creator.name" title="common.creator"]${(role.creator.fullname)!}[/@]
    [@b.col width="13%" property="enabled" title="common.status"][@enableInfo role.enabled/][/@]
    [@b.col width="14%" property="updatedAt" title="common.updatedAt"]${(role.updatedAt)!}[/@]
    [@b.col title="权限" width="12%"][@b.a target="_blank" href="/admin/security/permission/${role.id}/edit"]<span class="icon-action action-config"></span>功能权限[/@][/@]
    </tr>
  [/@]
[/@]
[@b.foot/]
