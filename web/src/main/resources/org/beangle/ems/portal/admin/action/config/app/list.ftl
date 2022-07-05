[#ftl]
[@b.head/]

[#assign types = {"web-service":"web服务","web-app":"web应用"} ]
[#macro enableInfo enabled]
[#if enabled]<img height="15px" width="15px" src="${b.static_url("bui","icons/16x16/actions/activate.png")}"/>${b.text("action.activate")}[#else]<font color="red"><img height="15px" width="15px" src="${b.static_url("bui","icons/16x16/actions/freeze.png")}"/>${b.text("action.freeze")}</font>[/#if]
[/#macro]

[@b.grid items=apps var="app"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="8%" property="indexno" title="分类号"/]
    [@b.col width="15%" property="name" title="名称"][@b.a href="!info?id=${app.id}"]${app.name!}[/@][/@]
    [@b.col width="17%" property="title" title="标题"/]
    [@b.col width="17%" property="enTitle" title="英文标题"/]
    [@b.col width="8%" property="group.title" title="分组"/]
    [@b.col width="8%" property="appType.title" title="类型"/]
    [@b.col width="15%" title="数据源" ]
      [#list app.datasources as  ds]<span title="${ds.db.serverName!} ${ds.db.databaseName!}">${ds.name}(${ds.maximumPoolSize})</span>[/#list]
    [/@]
    [@b.col width="7%" property="enabled" title="是否可用" ][@enableInfo app.enabled/][/@]
  [/@]
[/@]
[@b.foot/]
