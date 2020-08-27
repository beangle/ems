[#ftl]
[@b.head/]
[@b.grid items=blobMetas var="blobMeta" sortable="true"]
  [@b.gridbar]
   bar.addItem("${b.text("action.export")}",action.exportData("owner:所有者,name:文件名,sha:SHA摘要,filePath:存储路径,fileSize:文件大小,mediaType:文件类型,updatedAt:更新时间",null,"fileName=文件信息"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col  width="15%" property="owner" title="所有者"]
       <span style="font-size:0.9em">${blobMeta.owner}</span>
    [/@]
    [@b.col  width="20%" property="name" title="common.name"]
      <span [#if blobMeta.name?length>20]style="font-size:0.8em"[/#if]>
      [@b.a href="!info?id="+blobMeta.id target="_blank"]${blobMeta.name}[/@]
      </span>
    [/@]
    [@b.col  width="34%" property="filePath" title="路径"]
       <span style="font-size:0.8em" title="${blobMeta.profile.name}">${blobMeta.filePath}</span>
    [/@]
    [@b.col  width="8%" property="fileSize" title="大小"]
       ${(blobMeta.fileSize/1024.0)?string(".##")}K
    [/@]
    [@b.col  width="8%" property="mediaType" title="类型"/]
    [@b.col  width="10%" property="updatedAt" title="更新时间"]
      ${blobMeta.updatedAt?string("yy-MM-dd HH:mm")}
    [/@]
  [/@]
[/@]
[@b.foot/]
