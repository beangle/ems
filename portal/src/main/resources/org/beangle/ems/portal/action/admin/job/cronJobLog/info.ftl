[#ftl]
[@b.head/]
[@b.toolbar title="计划任务执行日志"]
  bar.addBack("${b.text("action.back")}");
  bar.addItem("${b.text("action.edit")}",action.edit());
[/@]
<table class="table table-sm table-detail">
  <tr>
    <td class="title" width="20%">计划任务</td>
    <td>${cronJobLog.job.name!}</td>
  </tr>
  <tr>
    <td class="title">执行时间</td>
    <td>${(cronJobLog.executeAt?string("yyyy-MM-dd HH:mm:ss"))!}</td>
  </tr>
  <tr>
    <td class="title">执行耗时</td>
    <td>[#if cronJobLog.duration??]${cronJobLog.duration.toMillis}毫秒[#else]-[/#if]</td>
  </tr>
  <tr>
    <td class="title">状态</td>
    <td>[#if cronJobLog.statusCode==0]成功[#elseif cronJobLog.statusCode==1]失败[#elseif cronJobLog.statusCode==2]运行中[#else]${cronJobLog.statusCode}[/#if]</td>
  </tr>
  <tr>
    <td class="title">结果文件路径</td>
    <td>${(cronJobLog.resultFilePath.getOrElse('-'))!}</td>
  </tr>
</table>
[@b.foot/]
