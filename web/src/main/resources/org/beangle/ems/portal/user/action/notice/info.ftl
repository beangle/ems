[#ftl]
[@b.head/]
[@b.toolbar title="公告信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
  <div class="box-body no-padding">
    <div class="mailbox-read-info">
      <h3>${notice.title}</h3>
      <h5>发自: ${notice.operator.name}(${notice.operator.code})
        <span class="mailbox-read-time pull-right">${(notice.publishedAt?string('yyyy-MM-dd'))!"尚未发布"}</span></h5>
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
[@b.foot/]
