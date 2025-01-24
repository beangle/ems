[#ftl]
[@b.head/]

[@b.toolbar title="添加/更新流程定义"]
  bar.addBack();
[/@]
[@b.form action=b.rest.save(flow) theme="list" name="ruleForm"]
  [@b.select name="flow.business.id" label="业务类型" value=flow.business! items=businesses required="true"/]
  [@b.textfield name="flow.code" label="代码" value=flow.code! maxlength="100" required="true"/]
  [@b.textfield name="flow.name" label="名称" value=flow.name! maxlength="100" required="true"/]
  [@b.textarea name="flow.envJson" label="环境数据" value=flow.dataJson! maxlength="2000" cols="80" rows="4" required="false" placeholder="json object 格式"/]
  [@b.textarea name="flow.Json" label="环境数据" value=flow.dataJson! maxlength="2000" cols="80" rows="4" required="false" placeholder="json object 格式"/]
  [@b.field label="流程步骤"]
    <table class="rulesTable" width="600px">
      <thead>
        <th style="width:20%">名称</th>
        <th style="width:20%">用户组</th>
        <th style="width:5%;"></th>
      </thead>
      <tbody>
        [@flowTasks tasks=flow.tasks?sort_by('idx')/]
        [#if flow.tasks?size <10]
        [#list flow.tasks?size .. 10 as taskCount]
        <tr>
          <td>
            <input id="task_name" name="${taskCount}.name" type="text" value="" maxlength="80" style="width:100%"/>
          </td>
          <td>
          <select name="${taskCount}.group.id" style="width:100%">
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

[#macro flowTasks tasks]
  [#if tasks?? && tasks?size>0]
    [#list tasks?sort_by("idx") as task]
      [#assign taskJSid = task_index/]
      <tr>
        <td>
          <input name="${taskJSid}.id" type="hidden" value="${task.id}"/>
          <input type="text" class="taskEditInput" name="${taskJSid}.name" value="${task.name}" title='名称'
                 maxlength="80" style="width:100%"/>
        </td>
        <td >
          <select name="${taskJSid}.group.id" style="width:100%">
            <option value="">...</option>
            [#list groups as group]
            <option value="${group.id}" [#if group=task.group]selected[/#if]>${group.name}</option>
            [/#list]
          </select>
        </td>
      </tr>
    [/#list]
  [/#if]
[/#macro]
