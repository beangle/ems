      [@b.card class="card-info card-primary card-outline"]
        [#assign title]<i class="fas fa-file-pdf"></i> 文档下载[/#assign]
        [@b.card_header class="border-transparent" title=title  minimal="true" closeable="true"]
        <a href="${b.base}/user/doc" target="_blank" class="float-right">更多...</a>
        [/@]
        [@b.card_body class="p-0"]
          <div class="table-responsive">
            <table class="table no-margin m-0  compact">
              <tbody>
              [#assign extMap={"xls":'xls.gif',"xlsx":'xls.gif',"docx":"doc.gif","doc":"doc.gif","pdf":"pdf.gif","zip":"zip.gif","":"generic.gif"}]
              [#list docs as doc]
              <tr>
                <td>
                  <image src="${b.static_url("ems","images/file/"+extMap[doc.name?keep_after_last(".")]?default("generic.gif"))}">&nbsp;
                  <a href="${b.base}/user/doc/${doc.id}" target="_blank">${doc.name}</a>
                </td>
                <td><span class="text-muted">${doc.updatedAt?string('MM-dd')}</span></td>
              </tr>
              [/#list]
              </tbody>
            </table>
          </div>
        [/@]
      [/@]
