[#ftl]
[@b.head/]
[@b.grid items=departs var="depart" sortable="false"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    <tr [#if depart??] id="${depart.indexno}"[/#if]>
    [@b.boxcol /]
    [@b.treecol title="代码、名称"][@b.a href="!info?id=${depart.id}"] ${depart.code} ${depart.name}[/@][/@]
    [@b.col width="8%" property="shortName" title="简称"]${depart.shortName!}[/@]
    [@b.col width="15%" property="beginOn" title="生效时间"]${depart.beginOn!}~${depart.endOn!}[/@]
    </tr>
  [/@]
[/@]
[@b.foot/]
