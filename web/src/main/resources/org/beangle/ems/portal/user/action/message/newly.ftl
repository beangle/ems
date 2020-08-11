[#list messages as message]
    <a class="dropdown-item" href="${ems.webapp}${base}/user/message/${message.id}" target="_blank">
      <div class="media">
        <img src="${avatarUrls[message.sender.code]}" class="img-size-32 mr-3 img-circle" alt="${message.sender.name}">
        <div class="media-body">
          <h4 class="dropdown-item-title">${message.sender.name}
          <span class="float-right text-sm text-muted"><i class="far fa-clock mr-1"></i>${message.sentAt?string('yy-MM-dd')}</span>
          </h4>
          <p class="text-sm">${message.title}</p>
        </div>
      </div>
    </a>
    <div class="dropdown-divider"></div>
  [/#list]
  <div>
    <a  class="float-right text-sm text-muted" href="${ems.webapp}${base}/user/message" target="_blank">
     <span>查看所有消息</span>
    </a>
  </div>
  <script>
     [#if  Parameters['callback']??]
        ${Parameters['callback']}(${messages.totalItems});
     [/#if]
  </script>
