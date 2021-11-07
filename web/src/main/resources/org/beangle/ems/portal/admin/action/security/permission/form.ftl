[#ftl]
[@b.head/]
[#include "../status.ftl"/]
<script type="text/javascript">
  beangle.load(["bui-tabletree"]);
</script>
<script type="text/javascript">
  function getIds(){
    return(getCheckBoxValue(document.getElementsByName("menuId")));
  }
  function save(){
    document.permissionForm.action="${b.url('!save')}";
    if(confirm("${b.text("alert.authoritySave",role.name)}")){
      document.permissionForm.submit();
    }
  }
  /**选中或取消资源*/
  function checkResource(ele){
    menuBoxId=ele.id;
    var stats = ele.checked;
    var num=0;
    var resourceBox;
    do{
      resourceBox = document.getElementById(menuBoxId+'_'+num);
      if(null==resourceBox) break;
      if(!resourceBox.disabled) resourceBox.checked = stats;
      num++;
    }while(resourceBox!=null);
  }
</script>
<table width="90%" align="center" class="text-sm">
<tr>
<td valign="top">
[@b.toolbar]
  bar.setTitle('角色-->菜单和资源权限');
  bar.addItem("${b.text("action.spread")}","bg.tabletree.displayAllRowsFor('meunPermissionTable',1);",'tree-folder');
  bar.addItem("${b.text("action.collapse")}","bg.tabletree.collapseAllRowsFor('meunPermissionTable',1);",'tree-folder-open');
  bar.addItem("${b.text("action.save")}",save,'save.png');
  function switchRole(form,roleId){
    form.action="${base}/admin/security/permission/{roleId}/edit".replace("{roleId}",roleId)
    form.submit();
  }
[/@]
[@b.form name="permissionForm" action="!edit"]
<table width="100%" class="searchTable" id="meunAuthorityTable">
  <tr>
    <td>
    角色:<select name="role.id" onchange="switchRole(this.form,this.value)" style="width:250px">
       [#list mngRoles?sort_by("indexno")! as r]
        <option value="${r.id}" [#if r.id=role.id]selected="selected"[/#if]>${r.indexno} ${r.name}</option>
       [/#list]
    </select>
    </td>
    <td class="title">
    应用:<select name="app.id" style="width:300px;" onchange="this.form.submit();">
      [#list apps as p]
      <option value="${p.id}" [#if current_app=p]selected="selected"[/#if]>${p.fullTitle}</option>
      [/#list]
      </select>
    </td>
    [#assign displayFreezen=Parameters['displayFreezen']!"false"/]
    <td><input name="displayFreezen" [#if displayFreezen='1'|| displayFreezen='on'|| displayFreezen='yes']checked="checked"[/#if] onclick="this.form.submit();" id="displayFreezen" type="checkbox"><label for="displayFreezen">显示冻结菜单</label></td>
  </tr>
</table>
<table width="100%" class="gridtable"  id="meunPermissionTable" style="border:solid 1px">
  <tbody>
  <tr class="gridhead">
  <th width="6%" class="gridselect"><input type="checkbox" onclick="bg.tabletree.selectAll(this,checkResource)"/></th>
  <th width="28%">${b.text("common.name")}</th>
  <th width="50%">可用资源</th>
  <th width="6%">${b.text("common.status")}</th>
  </tr>
  [#list mngMenus?sort_by("indexno") as menu]

  <tr class="grayStyle [#if !menu.enabled]ui-disabled[/#if]" id="${menu.indexno}">
    <td  class="gridselect">
      <input type="checkbox" id="checkbox_${menu_index}" onclick="bg.tabletree.select(this,checkResource)"  name="menuId" [#if parentMenus?seq_contains(menu)]checked="checked" disabled="disabled"[#else][#if (roleMenus?seq_contains(menu))]checked="checked"[/#if][/#if] value="${menu.id}">
    </td>
    <td>
    <div class="tree-tier${menu.depth}">
      [#if menu.children?size==0]
      <a href="#" class="tree-item"></a>[#rt]
      [#else]
      <a href="#" class="tree-folder-open" id="${menu.indexno}_folder" onclick="bg.tabletree.toggle(this);"></a>[#rt]
      [/#if]
      &nbsp;${menu.indexno} ${menu.title}
    </div>
    </td>
    <td>
      [#list menu.resources as resource]
        [#if mngResources?seq_contains(resource)]
        <input type="checkbox" name="resource.id" id="checkbox_${menu_index}_${resource_index}"[#if roleResources?seq_contains(resource)]checked="checked"[/#if] value="${resource.id}">[#rt]
        ${resource.title}
        [#if resource_index%3==1 && resource_has_next]<br/>[/#if]
        [/#if]
      [/#list]
    </td>
    <td align="center">[@shortEnableInfo menu.enabled/]</td>
  </tr>
  [/#list]
  </tbody>
</table>
[/@]
  </td>
  </tr>
</table>
[@b.foot/]
