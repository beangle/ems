[#ftl]
[@b.head/]
  [@b.grid id="flowTable" items=flows var="flow"]
    [@b.gridbar title="审核项目"]
      bar.addItem("${b.text("action.new")}",action.add());
      bar.addItem("${b.text("action.edit")}",action.edit());
      bar.addItem("${b.text("action.delete")}",action.remove());
    [/@]
    [@b.row]
      [@b.boxcol/]
      [@b.col title="业务类型" property="business.name" width="13%"/]
      [@b.col title="流程名称" property="name" width="15%"]
       [@b.a href="!info?id=${flow.id}" title="查看详情"]${flow.name}[/@]
      [/@]
      [@b.col title="流程步骤"]
        [#list flow.tasks?sort_by("idx") as task]${task.name}[#sep]&nbsp;[/#list]
      [/@]
    [/@]
  [/@]
