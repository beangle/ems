[#ftl]
[@b.head/]
[@b.toolbar title='错误日志明细']
bar.addBack("${b.text("action.back")}");
[/@]
<table class="table table-sm table-detail">
  <tr>
   <td class="title" width="10%">应用:</td>
   <td width="40%"> ${errorLog.app.name}</td>
   <td class="title" width="10%">发生时间:</td>
   <td>${errorLog.occurredAt?string('yyyy-MM-dd HH:mm:ss')}</td>
  </tr>
  <tr>
    <td class="title">异常:</td>
    <td>${errorLog.exceptionName}</td>
    <td class="title">消息:</td>
    <td>${errorLog.message}</td>
  </tr>
  <tr>
    <td class="title">操作人:</td>
    <td>${errorLog.username!}</td>
    <td class="title">入口:</td>
    <td>${errorLog.requestUrl}</td>
  </tr>
  <tr>
    <td class="title">参数:</td>
    <td colspan="3">
      <pre>${errorLog.params!}</pre>
    </td>
  </tr>
  <tr>
    <td class="title">异常堆栈:</td>
    <td colspan="3">
      <pre>${errorLog.stackTrace!}</pre>
    </td>
  </tr>
</table>
[@b.foot/]
