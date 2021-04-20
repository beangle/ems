[#ftl]
[@b.head/]
[@b.grid items=templates var="template" sortable="true"]
  [@b.gridbar]
   bar.addItem("${b.text("action.new")}",action.add());
   bar.addItem("${b.text("action.edit")}",action.edit());
   bar.addItem("${b.text("action.export")}",action.exportData("owner:所有者,name:文件名,sha:SHA摘要,filePath:存储路径,fileSize:文件大小,mediaType:文件类型,updatedAt:更新时间",null,"fileName=文件信息"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="8%" property="app.title" title="应用"/]
    [@b.col  width="52%" property="name" title="common.name"]
      <span [#if template.name?length>20]style="font-size:0.8em"[/#if]>
      [@b.a href="!info?id="+template.id target="_blank" title=template.filePath]${template.name}[/@]
      </span>
    [/@]
    [@b.col  width="5%" property="fileSize" title="大小"]
       ${(template.fileSize/1024.0)?string(".##")}K
    [/@]
    [@b.col  width="15%" property="mediaType" title="类型"/]
    [@b.col  width="10%" property="updatedAt" title="更新时间"]
      ${template.updatedAt?string("yy-MM-dd HH:mm")}
    [/@]
  [/@]
[/@]
[@b.foot/]
