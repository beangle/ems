[#ftl]
[@b.head/]
  [@b.grid id="flowTable" items=flows var="flow"]
    [@b.gridbar title="审核项目"]
      bar.addItem("${b.text("action.new")}",action.add());
      bar.addItem("${b.text("action.edit")}",action.edit());
      bar.addItem("${b.text("action.delete")}",action.remove());
      bar.addItem("复制",action.single('copy'));
    [/@]
    [@b.row]
      [@b.boxcol/]
      [@b.col title="业务类型" property="business.name" width="13%"/]
      [@b.col title="流程代码" property="code" width="15%"/]
      [@b.col title="流程名称" property="name" width="15%"]
        [@b.a href="!info?id=${flow.id}" title="查看详情"]${flow.name}[/@]
      [/@]
      [@b.col title="流程步骤"]
        [#list flow.activities?sort_by("idx") as act]${act.name}[#if act.guardComment??]<sup>${act.guardComment}</sup>[/#if][#sep]&nbsp;[/#list]
      [/@]
    [/@]
  [/@]
