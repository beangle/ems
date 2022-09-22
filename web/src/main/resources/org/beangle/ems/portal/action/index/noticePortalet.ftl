  [@b.card class="card-info card-primary card-outline"]
    [#assign title]<i class="far fa-bell"></i> 通知公告[/#assign]
    [@b.card_header class="border-transparent" title=title  minimal="true" closeable="true"]
    <a href="${webappBase}/user/notice" target="_blank" class="float-right">更多...</a>
    [/@]
    [@b.card_body class="p-0"]
      <div class="table-responsive">
        <table id="notice_table" class="table no-margin m-0 compact">
          <tbody>
          [#list notices as notice]
          <tr>
            <td><a href="${webappBase}/user/notice/${notice.id}" target="_blank"> ${notice.title}</a></td>
            <td><span class="text-muted">${notice.updatedAt?string('MM-dd')}</span></td>
          </tr>
          [/#list]
          </tbody>
        </table>
      </div>
    [/@]
  [/@]
