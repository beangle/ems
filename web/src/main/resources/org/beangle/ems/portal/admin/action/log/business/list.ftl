[#ftl]
[@b.head/]
[@b.grid items=businessLogs var="businessLog" sortable="true"]
  [@b.gridbar]
    [#if businessLogs.totalItems<10000]
    bar.addItem("${b.text("action.export")}",action.exportData("id:流水号,app.name:应用名,level.name:日志级别,operator:操作者,operateAt:操作时间,summary:操作内容摘要,entry:入口地址,agent:客户端,ip:IP,resources:资源,details:明细",null,"fileName=日志明细"));
    [/#if]
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col  width="90px" property="app.name" title="应用"]
       <span style="font-size:0.9em">${businessLog.app.title}</span>
    [/@]
    [@b.col  width="60px" property="level.name" title="级别"/]
    [@b.col  width="10%" property="operator" title="操作人"/]
    [@b.col property="summary" title="操作内容摘要"]
      [@b.a href="!info?id="+businessLog.id title="查看明细"]${businessLog.summary}[/@]
    [/@]
    [@b.col  width="15%" property="entry" title="入口"]
      <span style="font-size:0.8em" title="${businessLog.entry}">${businessLog.entry}</span>
    [/@]
    [@b.col  width="8%" property="ip" title="IP"]
       <span style="font-size:0.8em" title="${businessLog.agent}">${businessLog.ip}</span>
    [/@]
    [@b.col  width="10%" property="operateAt" title="操作时间"]
      ${businessLog.operateAt?string("yy-MM-dd HH:mm")}
    [/@]
  [/@]
[/@]
[@b.foot/]
