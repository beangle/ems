[#ftl]
[@b.head/]

 [#assign extMap={"xls":'application-vnd.ms-excel.png',"xlsx":'application-vnd.ms-excel.png',
  "doc":"application-msword.png","docx":"application-msword.png",
  "pdf":"application-pdf.png","zip":"application-zip.png",
  "rar":"application-zip.png","ppt":"application-vnd.ms-powerpoint.png"}]

[@b.grid items=docs var="doc"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col property="name" title="标题" style="text-align:left;padding-left:10px"]
      [@b.a href="!info?id=${doc.id}" target="_blank"]
        [#assign ext=doc.name?keep_after_last(".")/]
        <image style="width:16px" src="${b.static_url("bui","icons/48x48/mimetypes/"+extMap[ext]!"text-plain.png")}">
        ${doc.name!}
      [/@]
    [/@]
    [@b.col width="10%" property="app.title" title="应用"/]
    [@b.col width="17%" title="用户类别"]
      <span style="font-size:0.8em">[#list doc.categories as uc]${uc.name}[#if uc_has_next]&nbsp;[/#if][/#list]</span>
    [/@]
    [@b.col width="10%" property="uploadBy" title="上传人"]${doc.uploadBy.name}[/@]
    [@b.col width="10%" property="updatedAt" title="上传时间"]${doc.updatedAt?string("yy-MM-dd HH:mm")}[/@]
  [/@]
[/@]
[@b.foot/]
