[#ftl]
[@b.head/]
  [@b.grid items=processes var="process"]
    [@b.gridbar]
      bar.addItem("${b.text("action.delete")}",action.remove());
    [/@]
    [@b.row]
      [@b.boxcol/]
      [@b.col title="业务类型" property="flow.business.name" width="13%"/]
      [@b.col title="流程名称" property="flow.name" width="10%"]
       [@b.a href="!info?id=${process.id}" title="查看详情"]${process.flow.name}[/@]
      [/@]
      [@b.col title="发起人" property="initiator.id" width="15%"]
        [#if process.initiator??]${process.initiator.code} ${process.initiator.name}[#else]--[/#if]
      [/@]
      [@b.col title="流程步骤"]
        [#list process.tasks?sort_by("startAt") as task]${task.name}[#sep]&nbsp;[/#list]
      [/@]
      [@b.col title="当前状态" property="status" width="10%"/]
      [@b.col title="发起时间" property="startAt" width="13%"]
        ${process.startAt?string("yy-MM-dd HH:mm")}
      [/@]
    [/@]
  [/@]
