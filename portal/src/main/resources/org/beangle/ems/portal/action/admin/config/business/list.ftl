[#ftl]
[@b.head/]
[@b.grid items=businesses var="business"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="20%" property="code" title="代码"/]
    [@b.col property="name" title="名称"/]
  [/@]
[/@]
[@b.foot/]
