[#ftl]
[@b.head/]
[@b.grid items=reconfigs var="reconfig" sortable="true"]
  [@b.gridbar]
   bar.addItem("${b.text("action.new")}",action.add());
   bar.addItem("${b.text("action.edit")}",action.edit());
   bar.addItem("${b.text("action.delete")}",action.remove());
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="app.title" title="应用"/]
    [@b.col width="65%" property="remark" title="remark"/]
    [@b.col width="10%" property="updatedAt" title="更新时间"]
      ${reconfig.updatedAt?string("yy-MM-dd HH:mm")}
    [/@]
  [/@]
[/@]
[@b.foot/]
