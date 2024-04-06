[#ftl]
[@b.head/]
[@b.toolbar title='业务日志明细']
bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
   <td class="title" width="10%">应用:</td>
   <td class="content" width="40%"> ${businessLog.app.name}</td>
   <td class="title" width="10%">级别:</td>
   <td class="content" width="40%">${(businessLog.level.name)!}  </td>
  </tr>
  <tr>
    <td class="title">内容摘要:</td>
    <td class="content" colspan="3">${businessLog.summary}</td>
  </tr>
  <tr>
    <td class="title">操作人:</td>
    <td class="content">${businessLog.operator}</td>
    <td class="title">操作时间:</td>
    <td class="content">${businessLog.operateAt?string('yyyy-MM-dd HH:mm:ss')}</td>
  </tr>
  <tr>
    <td class="title">操作IP:</td>
    <td class="content">${businessLog.ip}</td>
    <td class="title">操作客户端:</td>
    <td class="content">${businessLog.agent}</td>
  </tr>
  <tr>
    <td class="title">入口:</td>
    <td class="content">${businessLog.entry}</td>
    <td class="title">操作资源:</td>
    <td class="content">${businessLog.resources}</td>
  </tr>
  <tr>
    <td class="title">详细、参数等:</td>
    <td class="content" colspan="3">
      <pre>${businessLog.details}</pre>
    </td>
  </tr>
</table>
[@b.foot/]
