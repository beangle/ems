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
  [@b.textfield name="flow.formUrl" label="表单URL" value=flow.formUrl! maxlength="300" required="true" style="width:400px"/]
  [@b.field label="流程步骤"]
    <table style="table-layout: fixed;" width="1000px">
      <thead>
        <tr>
          <th style="width:5%">步骤</th>
          <th style="width:15%">名称</th>
          <th style="width:20%">受理人(值/或表达式，多值使用半角逗号相隔)</th>
          <th style="width:15%">部门</th>
          <th style="width:15%">用户组</th>
          <th style="width:20%">条件</th>
          <th style="width:10%">条件说明</th>
        </tr>
      </thead>
      <tbody>
        [@flowActivities activities=flow.activities?sort_by('idx')/]
        [#if flow.activities?size <10]
        [#list flow.activities?size .. 10 as prefix]
        <tr>
          <td><input name="${prefix}.idx" type="text" value="" maxlength="10" style="width:100%" onchange="adjustIdx(this)"/></td>
          <td><input name="${prefix}.name" type="text" value="" maxlength="80" style="width:100%"/></td>
          <td><input name="${prefix}.assignees" value="" maxlength="300" style="width:100%"></td>
          <td><input name="${prefix}.depart" value="" maxlength="80" style="width:100%"></td>
          <td>
            <select name="${prefix}_group.id" style="width:100px" multiple="true">
              <option value="">...</option>
              [#list groups as group]
              <option value="${group.id}">${group.name}</option>
              [/#list]
            </select>
          </td>
          <td><input name="${prefix}.guard" value="" maxlength="500" style="width:100%"></td>
          <td><input name="${prefix}.guardComment" value="" maxlength="100" style="width:100%"></td>
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
          <input name="${prefix}.idx" type="text" value="${activity.idx}" maxlength="80" style="width:100%" onchange="adjustIdx(this)"/>
        </td>
        <td>
          <input type="text" class="activityEditInput" name="${prefix}.name" value="${activity.name}" title='名称'  maxlength="80" style="width:100%"/>
        </td>
        <td><input name="${prefix}.assignees" value="${activity.assignees!}" maxlength="300" style="width:100%"></td>
        <td><input name="${prefix}.depart" value="${activity.depart!}" maxlength="80" style="width:100%"></td>
        <td>
          <select name="${prefix}_group.id" style="width:100px" multiple="true">
            <option value="">...</option>
            [#list groups as group]
            <option value="${group.id}" [#if activity.groups?seq_contains(group)]selected[/#if]>${group.name}</option>
            [/#list]
          </select>
        </td>
        <td>
          <input type="text" class="activityEditInput" name="${prefix}.guard" value="${activity.guard!}" title='守护条件'  maxlength="500" style="width:100%"/>
        </td>
        <td>
          <input type="text" class="activityEditInput" name="${prefix}.guardComment" value="${activity.guardComment!}" title='守护条件说明'  maxlength="100" style="width:100%"/>
        </td>
      </tr>
    [/#list]
  [/#if]
[/#macro]
<script>
     jQuery(document).ready(function(){
       beangle.load(["chosen"],function(){
         jQuery("#ruleForm select").each(function(i,e){
           jQuery(e).chosen({placeholder_text_single:"...",no_results_text: "没有找到结果！",search_contains:true,allow_single_deselect:true,width:'150px'});
         });
       });
     });
     function adjustIdx(e){
       var row = e.parentNode.parentNode;
       if(e.value){
         var idx = parseInt(e.value);
         var tbody = row.parentNode;
         if(idx >=0 && idx < tbody.children.length){
           if(row != tbody.children[idx]){
             if(idx==0){
               tbody.prepend(row);
             }else if(idx==tbody.children.length-1){
               tbody.append(row);
             }else{
               tbody.removeChild(row);
               tbody.insertBefore(row,tbody.children[idx]);
             }
           }
         }
         var idx=0;
         for(var i=0;i < tbody.children.length;i++){
           var td = tbody.children[i].firstElementChild;
           jQuery(td).children("input[type='text']").val(idx)
           idx +=1;
         }
       }
     }
</script>
