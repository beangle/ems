[#ftl]
[@b.head/]
[@b.grid items=bundles var="bundle"]
  [@b.gridbar]
   bar.addItem("${b.text("action.new")}",action.add());
   bar.addItem("${b.text("action.edit")}",action.edit());
   bar.addItem("${b.text("action.delete")}",action.remove());
   bar.addItem("${b.text("action.export")}",action.exportData("app.title:应用,name:名称,locale:语言,texts:文本",null,"fileName=词条信息"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="8%" property="app.title" title="应用"/]
    [@b.col property="name" title="common.name"/]
    [@b.col width="5%" property="locale" title="语言"/]
    [@b.col width="60%" property="texts" title="词条"/]
  [/@]
[/@]
[@b.foot/]
