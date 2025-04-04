[@b.head/]
<div class="card card-primary card-outline">
    <div class="card-header">
      <h3 class="card-title">查看消息</h3>

      <div class="card-tools">
        <a href="#" class="btn btn-sm btn-box-tool" data-toggle="tooltip" title="" data-original-title="Previous"><i class="fa fa-chevron-left"></i></a>
        <a href="#" class="btn btn-sm btn-box-tool" data-toggle="tooltip" title="" data-original-title="Next"><i class="fa fa-chevron-right"></i></a>
      </div>
    </div>
    <div class="card-body" style="padding-top: 0px;">
      <div class="mailbox-read-info" style="padding-bottom: 0px;">
        <h5>${message.title}</h5>
        <h6>发自: ${message.sendFrom}
          <span class="mailbox-read-time float-right">${message.sentAt?string('yyyy-MM-dd HH:mm')}</span></h6>
      </div>
      <div class="mailbox-read-message">
        ${message.contents}
      </div>
    </div>

    <div class="card-footer">
      [#if message.sender??]
      <div class="float-right">
        <button type="button" class="btn btn-sm btn-default" onclick="bg.Go('${b.url("!editNew?recipient.code="+message.sender.code)}','messageList')"><i class="fa fa-reply"></i> 回复</button>
      </div>
      [/#if]
    </div>
  </div>
[@b.foot/]
