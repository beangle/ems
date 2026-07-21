[#ftl]
[@b.head/]
[#include "../status.ftl"/]
[@b.grid items=channels var="channel" sortable="false"]
  [@b.gridbar]
  action.addParam('channel.app.id',"${Parameters['channel.app.id']!}");
  bar.addItem("${b.text("action.new")}",action.add());
  bar.addItem("${b.text("action.edit")}",action.edit());
  bar.addItem("${b.text("action.delete")}",action.remove('删除时会级联删除该端下所有菜单，确定删除？'));
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col title="应用" width="18%" property="app.title"/]
    [@b.col title="前端类型" width="12%" property="channelType.title"/]
    [@b.col title="上下文地址" property="base"/]
    [@b.col title="嵌入方式" width="12%"]
      [#if channel.embedMode.name == "wujie"]微前端[#else]IFrame[/#if]
    [/@]
    [@b.col title="common.status" width="10%" property="enabled"][@enableInfo channel.enabled/][/@]
  [/@]
[/@]
[@b.foot/]
