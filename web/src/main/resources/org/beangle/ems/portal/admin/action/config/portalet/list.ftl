[#ftl]
[@b.head/]

[#macro enableInfo enabled]
[#if enabled]<img height="15px" width="15px" src="${b.static_url("bui","icons/16x16/actions/activate.png")}"/>${b.text("action.activate")}[#else]<font color="red"><img height="15px" width="15px" src="${b.static_url("bui","icons/16x16/actions/freeze.png")}"/>${b.text("action.freeze")}</font>[/#if]
[/#macro]

[@b.grid items=portalets var="portalet"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="8%" property="idx" title="序号"/]
    [@b.col width="10%" property="name" title="名称"][@b.a href="!info?id=${portalet.id}"]${portalet.name!}[/@][/@]
    [@b.col width="10%" property="title" title="标题"/]
    [@b.col width="6%" property="rowIndex" title="行"/]
    [@b.col width="6%" property="colspan" title="列数"/]
    [@b.col width="28%" property="url" title="网址"/]
    [@b.col width="6%" property="usingIframe" title="Iframe集成"/]
    [@b.col width="12%" title="适应用户" ]
      [#list portalet.categories as ca]${ca.name}[#sep]&nbsp;[/#list]
    [/@]
    [@b.col width="10%" property="enabled" title="是否可用" ][@enableInfo portalet.enabled/][/@]
  [/@]
[/@]
[@b.foot/]
