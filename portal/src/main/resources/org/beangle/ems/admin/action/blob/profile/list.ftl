[#ftl]
[@b.head/]
[@b.grid items=profiles var="profile" sortable="true"]
  [@b.gridbar title='系统资源']
  bar.addItem("${b.text("action.new")}",action.add());
  bar.addItem("${b.text("action.edit")}",action.edit());
  bar.addItem("${b.text("action.delete")}",action.remove());
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col property="name" title="common.name"/]
    [@b.col property="path" title="路径"/]
    [@b.col property="users" width="30%" title="应用"/]
    [@b.col property="namedBySha" title="sha命名"/]
    [@b.col property="publicDownload" title="公开下载"/]
    [@b.col property="publicList" title="公开浏览"/]
  [/@]
[/@]
[@b.foot/]
