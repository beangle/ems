[#ftl]
[@b.head/]
[@b.toolbar title="文档信息"]
  bar.addClose("${b.text("action.close")}");
[/@]
  <div class="box-body no-padding">
    <div class="mailbox-read-info">
      <h3>${doc.name}</h3>
      <h5>发自: ${doc.uploadBy.name}(${doc.uploadBy.code})
        <span class="mailbox-read-time pull-right"></span></h5>
    </div>
    <div class="mailbox-read-message">
      <ul>
      <li>大小:${(doc.fileSize/1024.0)?string(".##")}K</li>
      <li>面向:[#list doc.categories as uc]${uc.name}[#if uc_has_next]&nbsp;[/#if][/#list]</li>
      <li>下载：[@b.a href="!download?id="+doc.id target="_new"]${doc.name}[/@]</li>
      <li>发布时间:${doc.updatedAt?string('yyyy-MM-dd')}</li>
      </ul>
    </div>
  </div>
[@b.foot/]
