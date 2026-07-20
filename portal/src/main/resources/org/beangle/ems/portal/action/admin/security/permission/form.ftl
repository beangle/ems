[#ftl]
[@b.head/]
[#include "../status.ftl"/]
[#assign allowAllEnv=allowAllEnv!false /]
[#assign showEnvPanel=allowAllEnv || (appEnvs?size>0) /]
<script type="text/javascript">
  function getIds(){
    return(getCheckBoxValue(document.getElementsByName("menuId")));
  }
  function save(){
    [#if showEnvPanel && !allowAllEnv]
    var envChecked = document.querySelectorAll('input.env-choice:checked');
    if (envChecked.length == 0) {
      alert("请至少选择一个业务场景");
      return;
    }
    [#elseif showEnvPanel && allowAllEnv]
    var specific = document.querySelector('input[name="envScope"][value="specific"]');
    if (specific && specific.checked) {
      var envChecked = document.querySelectorAll('input.env-choice:checked');
      if (envChecked.length == 0) {
        alert("指定场景时请至少选择一个业务场景");
        return;
      }
    }
    [/#if]
    document.permissionForm.action="${b.url('!save')}";
    if(confirm("${b.text("alert.authoritySave",role.name)}")){
      document.permissionForm.submit();
    }
  }
  /** 选中或取消当前菜单行下的关联资源 */
  function checkResource(ele){
    var stats = ele.checked;
    var row = ele.closest ? ele.closest("tr") : ele.parentNode.parentNode;
    if (!row) return;
    var resourceBoxes = row.querySelectorAll('input[name="resource.id"]');
    for (var i = 0; i < resourceBoxes.length; i++) {
      if (!resourceBoxes[i].disabled) resourceBoxes[i].checked = stats;
    }
  }
  function toggleEnvScope(){
    // 仅针对「全部/指定」单选；应用已限定场景时没有该单选，场景应始终可选
    var specific = document.querySelector('input[name="envScope"][type="radio"][value="specific"]');
    var choices = document.querySelectorAll("input.env-choice");
    var enable = !specific || specific.checked;
    var anyChecked = false;
    for (var i = 0; i < choices.length; i++) {
      choices[i].disabled = !enable;
      if (choices[i].checked) anyChecked = true;
    }
    // 需要至少选一个时，若尚未勾选则帮用户全选
    if (enable && !anyChecked) {
      for (var i = 0; i < choices.length; i++) {
        choices[i].checked = true;
      }
    }
  }
</script>
<style type="text/css">
  #envPanel label,
  #envPanel label.form-label,
  #envPanel .form-check-label {
    margin-bottom: 0 !important;
    margin-top: 0 !important;
    margin-right: 10px;
    display: inline-block;
    vertical-align: middle;
  }
</style>
<table width="90%" align="center" class="text-sm">
<tr>
<td valign="top">
[@b.toolbar]
  bar.setTitle('角色-->菜单和资源权限');
  bar.addItem("${b.text("action.spread")}","beangle.ui.tabletree.displayAllRowsFor('meunPermissionTable',1);",'tree-folder');
  bar.addItem("${b.text("action.collapse")}","beangle.ui.tabletree.collapseAllRowsFor('meunPermissionTable',1);",'tree-folder-open');
  bar.addItem("${b.text("action.save")}",save,'save.png');
  function switchRole(form,roleId){
    form.action="${b.base}/portal/admin/security/permission/{roleId}/edit".replace("{roleId}",roleId)
    form.submit();
  }
[/@]
[@b.messages slash="3"/]
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
[#if showEnvPanel]
[#assign allEnvMode=allEnvMode!allowAllEnv /]
[#assign selectedEnvIdStrs=selectedEnvIdStrs![] /]
<div id="envPanel" style="margin:6px 0;padding:6px 0;">
  <strong>业务场景：</strong>
  [#if allowAllEnv]
  <label style="margin-bottom:0;margin-right:10px;"><input type="radio" name="envScope" value="all" [#if allEnvMode]checked="checked"[/#if] onclick="toggleEnvScope()"/> 不限场景</label>
  <label style="margin-bottom:0;margin-right:10px;"><input type="radio" name="envScope" value="specific" [#if !allEnvMode]checked="checked"[/#if] onclick="toggleEnvScope()"/> 指定场景</label>
  [#else]
  <input type="hidden" name="envScope" value="specific"/>
  [/#if]
  <span id="envChoices">
    [#list appEnvs as env]
    [#assign envChecked=selectedEnvIdStrs?seq_contains(env.id?string) /]
    [#-- 应用限定场景且尚无选中时，默认全选，方便通过「至少选一个」校验 --]
    [#if !allowAllEnv && selectedEnvIdStrs?size==0][#assign envChecked=true /][/#if]
    <label style="margin-bottom:0;margin-right:10px;"><input type="checkbox" class="env-choice" name="env.id" value="${env.id}" [#if envChecked]checked="checked"[/#if] [#if allowAllEnv && allEnvMode]disabled="disabled"[/#if]/> ${env.name}</label>
    [/#list]
  </span>
</div>
<script type="text/javascript">toggleEnvScope();</script>
[/#if]
<table width="100%" class="grid-table"  id="meunPermissionTable">
  <thead>
    <tr class="grid-head">
      <th class="grid-select" width="30px"><input type="checkbox" onclick="beangle.ui.tabletree.selectAll(this,checkResource)"/></th>
      <th width="28%">${b.text("common.name")}</th>
      <th>可用资源</th>
      <th width="6%">${b.text("common.status")}</th>
    </tr>
  </thead>
  <tbody>
  [#list mngMenus?sort_by("indexno") as menu]
  [#assign isParent=parentMenus?seq_contains(menu)/]
  <tr class="grayStyle [#if !menu.enabled]ui-disabled[/#if]" id="${menu.indexno}">
    <td  class="grid-select">
      <input type="checkbox" id="checkbox_${menu_index}" onclick="beangle.ui.tabletree.select(this,checkResource)"  name="menuId" [#if isParent]checked="checked" disabled="disabled"[#else][#if (roleMenus?seq_contains(menu))]checked="checked"[/#if][/#if] value="${menu.id}">
    </td>
    <td>
    <div class="tree-tier${menu.depth}">
      [#if menu.children?size==0]
      <a href="#" class="tree-item"></a>[#rt]
      [#else]
      <a href="#" class="tree-folder-open" id="${menu.indexno}_folder" onclick="beangle.ui.tabletree.toggle(this);"></a>[#rt]
      [/#if]
      &nbsp;${menu.indexno} ${menu.name}
    </div>
    </td>
    <td style="padding:2px 4px;">
      [#list menu.resources as resource]
        [#if mngResources?seq_contains(resource)]
        <input type="checkbox" name="resource.id" id="checkbox_${menu_index}_${resource_index}"[#if roleResources?seq_contains(resource)]checked="checked"[/#if] value="${resource.id}">[#rt]
        ${resource.title}
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
