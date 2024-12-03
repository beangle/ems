[#ftl]
[@b.head/]
  [@b.grid items=ruleMetas var="ruleMeta"]
    [@b.gridbar title="规则列表"]
      bar.addItem("${b.text("action.new")}",action.add());
      bar.addItem("${b.text("action.edit")}",action.edit());
      bar.addItem("${b.text("action.delete")}",action.remove());
    [/@]
    [@b.row]
      [@b.boxcol/]
      [@b.col title="业务类型" property="business.name" width="10%"/]
      [@b.col title="规则名称" property="name" width="20%"/]
      [@b.col title="规则标题" property="title" width="20%"]
        [@b.a href="!info?id=${ruleMeta.id}" title="查看详情"]${ruleMeta.title}[/@]
      [/@]
      [@b.col title="规则描述" property="description" /]
      [@b.col title="参数描述" width="20%"]
        [#if ruleMeta.params? size == 0]
          无
        [#else]
          [#list ruleMeta.params?sort_by('name') as param]
            ${param.name} : ${param.title}
            [#if param_has_next]<br>[/#if]
          [/#list]
        [/#if]
      [/@]
    [/@]
  [/@]
