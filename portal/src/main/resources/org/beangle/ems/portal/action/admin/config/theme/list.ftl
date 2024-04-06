[#ftl]
[@b.head/]
[@b.grid items=themes var="theme"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col property="name" title="名称"/]
    [@b.col width="15%" property="primaryColor" title="超链接字体颜色"]
      <div style="background-color:${theme.primaryColor!}">&nbsp;</div>
    [/@]
    [@b.col width="15%" property="navbarBgColor" title="导航区背景"]
      <div style="background-color:${theme.navbarBgColor}">&nbsp;</div>
    [/@]
    [@b.col width="15%" property="searchBgColor" title="查询区背景"]
      <div style="background-color:${theme.searchBgColor}">&nbsp;</div>
    [/@]
    [@b.col width="15%" property="gridbarBgColor" title="表格工具栏背景"]
      <div style="background-color:${theme.gridbarBgColor}">&nbsp;</div>
    [/@]
    [@b.col width="15%" property="gridBorderColor" title="表格边框颜色"]
      <div style="background-color:${theme.gridBorderColor}">&nbsp;</div>
    [/@]
    [@b.col width="10%" property="enabled" title="是否启用"]${theme.enabled?c}[/@]
  [/@]
[/@]
[@b.foot/]
