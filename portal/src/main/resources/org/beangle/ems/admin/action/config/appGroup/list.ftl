[#ftl]
[@b.head/]
[@b.grid items=appGroups var="appGroup"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="indexno" title="分类号"/]
    [@b.col width="40%" property="name" title="名称"/]
    [@b.col width="40%" property="title" title="标题"/]
  [/@]
[/@]
[@b.foot/]
