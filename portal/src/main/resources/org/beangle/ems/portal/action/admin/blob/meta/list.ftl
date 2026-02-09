[#ftl]
[@b.head/]
[#assign mediaTypes={'application/msword':'fa-file-word',"application/pdf":'fa-file-pdf',
                     "application/vnd.ms-excel":'fa-file-excel',"application/zip":'fa-file-zipper',
                     "application/x-rar-compressed":'fa-file-zipper',
                     "application/vnd.openxmlformats-officedocument.wordprocessingml.document":'fa-file-word',
                     "application/x-7z-compressed":'fa-file-zipper'}/]
[@b.grid items=blobMetas var="blobMeta" sortable="true"]
  [@b.gridbar]
    bar.addItem("${b.text("action.export")}",action.exportData("owner:所有者,name:文件名,sha:SHA摘要,filePath:存储路径,fileSize:文件大小,mediaType:文件类型,updatedAt:更新时间",null,"fileName=文件信息"));
    bar.addItem("删除",action.remove());
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="12%" property="owner" title="所有者"]
       <span style="font-size:0.8em">${blobMeta.owner}</span>
    [/@]
    [@b.col property="name" title="common.name"]
      <span [#if blobMeta.name?length>20]style="font-size:0.8em"[/#if]>
      [@b.a href="!info?id="+blobMeta.id target="_blank"]${blobMeta.name}[/@]
      </span>
    [/@]
    [@b.col  width="38%" property="filePath" title="路径"]
       <div style="font-size:0.8em" title="${blobMeta.profile.name} ${blobMeta.filePath}" class="text-ellipsis">${blobMeta.filePath}</div>
    [/@]
    [@b.col  width="8%" property="fileSize" title="大小"]
       ${(blobMeta.fileSize/1024.0)?string(".##")}K
    [/@]
    [@b.col  width="5%" property="mediaType" title="类型"]
      <span title="${blobMeta.mediaType}">
        [#if mediaTypes[blobMeta.mediaType]??]
           <i class="fa-solid ${mediaTypes[blobMeta.mediaType]}"></i>
        [#else]
          [#if blobMeta.mediaType?starts_with('image')]<i class="fas fa-file-image"></i>[#else]
          <i class="fa-solid fa-file"></i>
          [/#if]
        [/#if]
      </span>
    [/@]
    [@b.col  width="10%" property="updatedAt" title="更新时间"]
      ${blobMeta.updatedAt?string("yy-MM-dd HH:mm")}
    [/@]
  [/@]
[/@]
[@b.foot/]
