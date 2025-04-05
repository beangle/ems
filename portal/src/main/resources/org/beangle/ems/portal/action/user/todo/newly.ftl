[#list todoes as todo]
    <a class="dropdown-item" href="${todo.url}" target="_blank">
      <div class="media">
        <div class="media-body">
          <h4 class="dropdown-item-title">${todo.title}
          <span class="float-right text-sm text-muted"><i class="far fa-clock mr-1"></i>${todo.updatedAt?string('yy-MM-dd')}</span>
          </h4>
          <p class="text-sm">${todo.contents}</p>
        </div>
      </div>
    </a>
    <div class="dropdown-divider"></div>
  [/#list]
  <div>
    <a  class="float-right text-sm text-muted" href="${ems.webapp}${b.base}/user/todo" target="_blank">
     <span>查看所有代办</span>
    </a>
  </div>
  <script>
     [#if  Parameters['callback']??]
        ${Parameters['callback']}(${todoes.totalItems});
     [/#if]
  </script>
