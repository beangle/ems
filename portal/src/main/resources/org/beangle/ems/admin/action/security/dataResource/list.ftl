[#ftl]
[@b.head/]
[#include "../status.ftl"/]
[@b.grid items=resources var="resource" sortable="true"]
  [@b.gridbar title='数据资源']
  bar.addItem("${b.text("action.new")}",action.add());
  bar.addItem("${b.text("action.edit")}",action.edit());
  bar.addItem("${b.text("action.delete")}",action.remove());
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col  width="30%" property="name" align="left" style="padding-left:10px" title="common.name"/]
    [@b.col  width="15%" property="title" title="common.title"]${(resource.title)!}[/@]
    [@b.col  width="15%" property="domain.name" title="领域"/]
    [@b.col  width="35%" property="typeName" align="left" title="类型"/]
  [/@]
[/@]
[@b.foot/]
