[#ftl]
[@b.head/]
  [@b.grid id="ruleTable" items=rules var="rule"]
    [@b.gridbar title="审核项目"]
      bar.addItem("${b.text("action.new")}",action.add());
      bar.addItem("${b.text("action.edit")}",action.edit());
      bar.addItem("${b.text("action.delete")}",action.remove());
    [/@]
    [@b.row]
      [@b.boxcol/]
      [@b.col title="业务类型" property="meta.business.name" width="15%"/]
      [@b.col title="场景配置ID" property="profileId" width="10%"/]
      [@b.col title="规则名称" property="name" width="20%"]
       [@b.a href="!info?id=${rule.id}" title="查看详情"]${rule.name}[/@]
      [/@]
      [@b.col title="规则描述" property="meta.description" width="25%"/]
      [@b.col title="参数"]
        [#if rule.params?size == 0]
        无
        [#else]
        [#list rule.params?sort_by(['meta','name']) as rcp]
          ${rcp.meta.title} = ${rcp.contents!}
          [#if rcp_has_next]<br>[/#if]
        [/#list]
        [/#if]
      [/@]
      [@b.col title="是否有效" property="enabled" width="8%"]${rule.enabled?string("是","否")}[/@]
    [/@]
  [/@]
