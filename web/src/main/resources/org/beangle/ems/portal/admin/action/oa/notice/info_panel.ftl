[#ftl/]
  <div class="box-body no-padding">
    <div class="mailbox-read-info">
      <h3>${notice.title}</h3>
      <h6>发自: ${notice.issuer}
        <span class="mailbox-read-time pull-right">${notice.createdAt?string('yyyy-MM-dd')}</span></h6>
    </div>
    <div class="mailbox-read-message">
      ${notice.contents}
    </div>
    [#if notice.docs?size>0]
    <div class="mailbox-read-message">
      <ul>附件列表
      [#list notice.docs as doc]
        [#if doc.image]
          <li><img src="${doc.url}" style="height:300px"/></li>
        [#else]
          <li>[@b.a href="doc!download?id="+doc.id target="_new"]${doc.name}[/@]</li>
        [/#if]
      [/#list]
      </ul>
    </div>
    [/#if]
  </div>
