[#ftl]
[@b.head/]
<div class="container" style="background-color: white;box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);min-height: 800px;">
  [@b.toolbar title="公告信息"]
    bar.addBackOrClose();
  [/@]
  <div class="box-body no-padding">
    <div class="mailbox-read-info">
      <h3>${notice.title}</h3>
      <h6>发自: ${notice.issuer}
        <span class="mailbox-read-time pull-right">${(notice.publishedAt?string('yyyy-MM-dd'))!"尚未发布"}</span></h6>
    </div>
    <div class="mailbox-read-message">
      ${notice.contents}
    </div>
    [#if notice.docs?size>0]
    <div class="mailbox-read-message">
      <ul>附件列表
      [#list notice.docs as doc]
      <li>[@b.a href="doc!info?id="+doc.id target="_new"]${doc.name}[/@]</li>
      [/#list]
      </ul>
    </div>
    [/#if]
  </div>
</div>
[@b.foot/]
