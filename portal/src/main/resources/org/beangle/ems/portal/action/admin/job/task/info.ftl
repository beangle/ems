[#ftl]
[@b.head/]
[@b.toolbar title="计划任务信息"]
  bar.addBack("${b.text("action.back")}");
  bar.addItem("${b.text("action.edit")}",action.edit());
[/@]
<table class="table table-sm table-detail">
  <tr>
    <td class="title" width="20%">名称</td>
    <td>${task.name!}</td>
  </tr>
  <tr>
    <td class="title">目标</td>
    <td>${task.target!}</td>
  </tr>
  <tr>
    <td class="title">描述</td>
    <td>${task.description!}</td>
  </tr>
  <tr>
    <td class="title">任务内容</td>
    <td><pre>${task.command!}</pre></td>
  </tr>
  <tr>
    <td class="title">Cron表达式</td>
    <td>${task.expression!}</td>
  </tr>
  <tr>
    <td class="title">是否启用</td>
    <td>[#if task.enabled!]启用[#else]禁用[/#if]</td>
  </tr>
  <tr>
    <td class="title">上次执行时间</td>
    <td>${(task.lastExecuteAt?string("yyyy-MM-dd HH:mm:ss"))!'-'}</td>
  </tr>
  <tr>
    <td class="title">执行耗时</td>
    <td>[#if task.duration??]${task.duration.toMillis}毫秒[#else]-[/#if]</td>
  </tr>
  <tr>
    <td class="title">更新时间</td>
    <td>${(task.updatedAt?string("yyyy-MM-dd HH:mm:ss"))!'-'}</td>
  </tr>
</table>
[@b.foot/]
