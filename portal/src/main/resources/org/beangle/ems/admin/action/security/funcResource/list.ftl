[#ftl]
[@b.head/]
[#include "scope.ftl"/]
[#include "../status.ftl"/]
[@b.grid items=resources var="resource" sortable="true"]
  [@b.gridbar title='系统资源']
  function activate(enabled){return action.multi('activate','确定操作?','&enabled='+enabled);}
  bar.addItem("${b.text("action.new")}",action.add());
  bar.addItem("${b.text("action.edit")}",action.edit());
  bar.addItem("${b.text("action.freeze")}",activate(0),'action-freeze');
  bar.addItem("${b.text("action.activate")}",activate(1),'action-activate');
  bar.addItem("${b.text("action.delete")}",action.remove());
  bar.addItem("${b.text("action.export")}",action.exportData("title:common.title,name:common.name,scope:可见范围,enabled:common.status,remark:common.remark",null,"fileName=资源信息"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col  width="37%" property="name" style="text-align:left;padding-left:10px"title="common.name"]
      [@b.a href="!info?id=${resource.id}"]${resource.name}[/@]
    [/@]
    [@b.col  width="28%" property="title" title="common.title"]${(resource.title)!}[/@]
    [@b.col  width="10%" property="scope" title="可见范围"][@resourceScope resource.scope/][/@]
    [@b.col  width="10%" property="enabled" title="common.status"][@enableInfo resource.enabled/][/@]
    [@b.col  width="10%" property="remark" title="common.remark"/]
  [/@]
[/@]
[@b.foot/]
