[#ftl]
[@b.head/]
[@b.toolbar title="计划任务执行日志"]
  bar.addBack("${b.text("action.back")}");
  bar.addItem("${b.text("action.edit")}",action.edit());
[/@]
<style>
pre{
  counter-reset:line;
}
pre.output > code > span:before {
  content: counter(line,decimal-leading-zero) " ";
  counter-increment: line;
  color: gray;
}

</style>
<table class="table table-sm table-detail">
  <tr>
    <td class="title" width="20%">计划任务</td>
    <td>${log.task.name!}</td>
  </tr>
  <tr>
    <td class="title">执行时间</td>
    <td>${(log.executeAt?string("yyyy-MM-dd HH:mm:ss"))!}</td>
  </tr>
  <tr>
    <td class="title">执行耗时</td>
    <td>[#if log.duration??]${log.duration.toMillis}毫秒[#else]-[/#if]</td>
  </tr>
  <tr>
    <td class="title">状态</td>
    <td>[#if log.statusCode==0]成功[#elseif log.statusCode==1]失败[#elseif log.statusCode==2]运行中[#else]${log.statusCode}[/#if]</td>
  </tr>
  <tr>
    <td class="title">结果</td>
    <td><pre class="output"><code>
[#list result as r]<span>${r}</span>
[/#list]
    </code></pre></td>
  </tr>
</table>
[@b.foot/]
