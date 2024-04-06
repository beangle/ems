[@b.head/]
<div class="content-header">
  <div class="container-fluid">
    <div class="row mb-2">
      <div class="col-sm-6">
        <h1 class="m-0 text-dark">欢迎 <small>${user.name}</small></h1>
      </div><!-- /.col -->
      <div class="col-sm-6">
        <ol class="breadcrumb  float-sm-right">
          <li class="breadcrumb-item"><i class="fas fa-tachometer-alt" style="margin-top: 6px;"></i> 首页</li>
          <li class="breadcrumb-item active">欢迎</li>
        </ol>
      </div><!-- /.col -->
    </div><!-- /.row -->
  </div>
</div>

<div class="row content" style="margin:0px">
  <section class="col-lg-7">
  [@b.card class="card-info card-primary card-outline"]
    [#assign title]<i class="far fa-bell"></i> 通知公告[/#assign]
    [@b.card_header class="border-transparent" title=title  minimal="true" closeable="true"]
    <a href="${b.base}/user/notice?notice.app.name=${Parameters['app']}" target="_blank" class="float-right">更多...</a>
    [/@]
    [@b.card_body class="p-0"]
      <div class="table-responsive">
        <table id="notice_table" class="table no-margin m-0 compact">
          <tbody>
          [#list notices as notice]
          <tr>
            <td><a href="${b.base}/user/notice/${notice.id}" target="_blank"> ${notice.title}</a></td>
            <td><span class="text-muted">${notice.updatedAt?string('MM-dd')}</span></td>
          </tr>
          [/#list]
          </tbody>
        </table>
      </div>
    [/@]
  [/@]

  </section>
  <section class="col-lg-5">
      [@b.card class="card-info card-primary card-outline"]
        [#assign title]<i class="fas fa-file-pdf"></i> 文档下载[/#assign]
        [@b.card_header class="border-transparent" title=title  minimal="true" closeable="true"]
        <a href="${b.base}/user/doc?doc.app.name=${Parameters['app']}" target="_blank" class="float-right">更多...</a>
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
  </section>
[@b.foot/]
