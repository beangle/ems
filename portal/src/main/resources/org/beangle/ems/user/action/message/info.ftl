[@b.head/]
<div class="card card-primary card-outline">
    <div class="card-header">
      <h3 class="card-title">查看消息</h3>

      <div class="card-tools">
        <a href="#" class="btn btn-box-tool" data-toggle="tooltip" title="" data-original-title="Previous"><i class="fa fa-chevron-left"></i></a>
        <a href="#" class="btn btn-box-tool" data-toggle="tooltip" title="" data-original-title="Next"><i class="fa fa-chevron-right"></i></a>
      </div>
    </div>
    <div class="card-body">
      <div class="mailbox-read-info">
        <h5>${message.title}</h5>
        <h6>发自: ${message.sender.name}(${message.sender.code})
          <span class="mailbox-read-time float-right">${message.sentAt?string('yyyy-MM-dd HH:mm')}</span></h6>
      </div>
      <div class="mailbox-read-message">
        ${message.content}
      </div>
    </div>

    <div class="card-footer">
      <div class="float-right">
        <button type="button" class="btn btn-default" onclick="bg.Go('${b.url("!editNew?recipient.code="+message.sender.code)}','messageList')"><i class="fa fa-reply"></i> Reply</button>
      </div>
    </div>
  </div>
[@b.foot/]
