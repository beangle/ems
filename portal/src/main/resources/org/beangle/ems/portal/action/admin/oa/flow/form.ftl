[#ftl]
[@b.head/]

[@b.toolbar title="添加/更新流程定义"]
  bar.addBack();
[/@]
[@b.form action=b.rest.save(flow) theme="list" name="ruleForm"]
  [@b.select name="flow.business.id" label="业务类型" value=flow.business! items=businesses required="true"/]
  [@b.textfield label="场景配置ID" required="true" maxLength="150px" name="flow.profileId" value=flow.profileId!/]
  [@b.textfield name="flow.code" label="代码" value=flow.code! maxlength="100" required="true"/]
  [@b.textfield name="flow.name" label="名称" value=flow.name! maxlength="100" required="true"/]
  [@b.textarea name="flow.guardJson" label="先决条件" value=flow.guardJson! maxlength="300" cols="80" rows="4" required="false" placeholder="json object 格式"/]
  [@b.textarea name="flow.envJson" label="初始数据" value=flow.envJson! maxlength="2000" cols="80" rows="4" required="false" placeholder="json object 格式"/]
  [@b.field label="流程步骤"]
    <table class="rulesTable" width="800px">
      <thead>
        <tr>
          <th style="width:15%">名称</th>
          <th style="width:10%">受理人</th>
          <th style="width:20%">可选用户</th>
          <th style="width:15%">部门</th>
          <th style="width:20%">用户组</th>
        </tr>
      </thead>
      <tbody>
        [@flowActivities activities=flow.activities?sort_by('idx')/]
        [#if flow.activities?size <10]
        [#list flow.activities?size .. 10 as prefix]
        <tr>
          <td>
            <input name="${prefix}.name" type="text" value="" maxlength="80" style="width:100%"/>
          </td>
          <td><input name="${prefix}.assignee" value="" maxlength="80" style="width:100%"></td>
          <td><input name="${prefix}.candidates" value="" maxlength="80" style="width:100%"></td>
          <td><input name="${prefix}.depart" value="" maxlength="80" style="width:100%"></td>
          <td>
          <select name="${prefix}_group.id" style="width:100%" multiple="true">
            <option value="">...</option>
            [#list groups as group]
            <option value="${group.id}">${group.name}</option>
            [/#list]
          </select>
          </td>
        </tr>
        [/#list]
        [/#if]
      </tbody>
    </table>
  [/@]
  [@b.formfoot]
    [@b.reset/] [@b.submit/]
  [/@]
[/@]

[#macro flowActivities activities]
  [#if activities?? && activities?size>0]
    [#list activities?sort_by("idx") as activity]
      [#assign prefix = activity_index/]
      <tr>
        <td>
          <input name="${prefix}.id" type="hidden" value="${activity.id}"/>
          <input type="text" class="activityEditInput" name="${prefix}.name" value="${activity.name}" title='名称'
                 maxlength="80" style="width:100%"/>
        </td>
        <td><input name="${prefix}.assignee" value="${activity.assignee!}" maxlength="80" style="width:100%"></td>
        <td><input name="${prefix}.candidates" value="${activity.candidates!}" maxlength="80" style="width:100%"></td>
        <td><input name="${prefix}.depart" value="${activity.depart!}" maxlength="80" style="width:100%"></td>
        <td>
          <select name="${prefix}_group.id" style="width:100%" multiple="true">
            <option value="">...</option>
            [#list groups as group]
            <option value="${group.id}" [#if activity.groups?seq_contains(group)]selected[/#if]>${group.name}</option>
            [/#list]
          </select>
        </td>
      </tr>
    [/#list]
  [/#if]
[/#macro]
<script>
     jQuery(document).ready(function(){
       beangle.load(["chosen"],function(){
         jQuery("#ruleForm select").each(function(i,e){
           jQuery(e).chosen({placeholder_text_single:"...",no_results_text: "没有找到结果！",search_contains:true,allow_single_deselect:true,width:'300px'});
         });
       });

     });
</script>
