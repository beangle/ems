[#ftl]
[@b.head/]
[#assign extMap={"xls":'xls.gif',"xlsx":'xls.gif',"docx":"doc.gif","doc":"doc.gif","pdf":"pdf.gif","zip":"zip.gif","":"generic.gif"}]
[@b.grid items=files var="file" sortable="true"]
  [@b.gridbar]
   bar.addItem("${b.text("action.new")}",action.add());
   bar.addItem("${b.text("action.edit")}",action.edit());
   bar.addItem("${b.text("action.delete")}",action.remove());
   bar.addItem("${b.text("action.export")}",action.exportData("app.title:应用,name:文件路径,filePath:存储路径,fileSize:文件大小,mediaType:文件类型,updatedAt:更新时间",null,"fileName=模板信息"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="8%" property="app.title" title="应用"/]
    [@b.col property="name" title="common.name"]
      <span [#if file.name?length>20]style="font-size:0.8em"[/#if]>
      [@b.a href="!info?id="+file.id target="_blank" title=file.filePath]${file.name}[/@]
      </span>
    [/@]
    [@b.col  width="5%" property="fileSize" title="大小"]
       ${(file.fileSize/1024.0)?string(".##")}K
    [/@]
    [@b.col  width="10%" property="mediaType" title="类型"]
      <image src="${b.static_url("ems","images/file/"+extMap[file.filePath?keep_after_last(".")]?default("generic.gif"))}">&nbsp;
    [/@]
    [@b.col  width="10%" property="updatedAt" title="更新时间"]
      ${file.updatedAt?string("yy-MM-dd HH:mm")}
    [/@]
  [/@]
[/@]
[@b.foot/]
