[#ftl]
[@b.head/]
[@b.grid items=roots var="root"]
  [@b.gridbar ]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.edit")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove());
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="user.code" width="15%"/]
    [@b.col property="user.name" width="15%"/]
    [@b.col property="beginOn" title="有效期" width="20%"]${root.beginOn!}~${root.endOn!}[/@]
    [@b.col property="updatedAt"/]
  [/@]
[/@]
[@b.foot/]
