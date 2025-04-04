[#ftl]
[@b.head/]
<div class="container-fluid">
  [@b.grid items=notices var="notice"]
    [@b.row]
      [@b.col width="5%" title="序号"]${notice_index+1}[/@]
      [@b.col property="title" title="标题"]
        [@b.a href="!info?id=${notice.id}"]
        [#if notice.docs?size>0]<image src="${b.static_url('bui',"icons/16x16/actions/paperclip.png")}" />[/#if]
        ${notice.title!}
        [/@]
      [/@]
      [@b.col width="15%" property="app.title" title="应用"/]
      [@b.col width="15%" property="beginOn" title="有效期"]${notice.beginOn?string("MM-dd")}~${notice.endOn?string("MM-dd")}[/@]
      [@b.col width="10%" property="publishedAt" title="发布日期"]${notice.publishedAt?string("yy-MM-dd")}[/@]
      [@b.col width="10%" property="issuer" title="发布人"]${notice.issuer}[/@]
    [/@]
  [/@]
</div>
[@b.foot/]
