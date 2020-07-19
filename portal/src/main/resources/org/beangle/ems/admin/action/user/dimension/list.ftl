[#ftl]
[@b.head/]
<div id="restrict_metas">
[@b.grid items=dimensions var="dimension"]
  [@b.gridbar ]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.edit")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove());
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="name" width="10%"/]
    [@b.col property="title" width="7%"/]
    [@b.col property="valueType" width="7%"/]
    [@b.col property="source" width="56%"]
      <span style="overflow:hidden;font-size:0.8em">${dimension.source}</span>
    [/@]
    [@b.col property="multiple" width="7%"]${dimension.multiple?string('是','否')}[/@]
    [@b.col property="properties" width="10%"][#if dimension.keyName??]${dimension.keyName},[/#if]${dimension.properties!}[/@]
  [/@]
[/@]
</div>
[@b.foot/]
