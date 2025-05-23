[#ftl]
[@b.head/]
<div class="container-fluid">
  [@b.grid items=docs var="doc" sortable="false"]
    [@b.row]
      [@b.col width="5%" title="序号"]${doc_index+1}[/@]
      [@b.col width="48%" property="name" title="标题"][@b.a target="_blank" href="!info?id=${doc.id}"]${doc.name!}[/@][/@]
      [@b.col width="10%" property="app.title" title="应用"/]
      [@b.col width="17%" title="用户类别"]
      <span style="font-size:0.8em">[#list doc.categories as uc]${uc.name}[#if uc_has_next]&nbsp;[/#if][/#list]</span>
      [/@]
      [@b.col width="10%" property="uploadBy" title="上传人"]${doc.uploadBy.name}[/@]
      [@b.col width="10%" property="updatedAt" title="上传时间"]${doc.updatedAt?string("yy-MM-dd HH:mm")}[/@]
    [/@]
  [/@]
</div>
[@b.foot/]
