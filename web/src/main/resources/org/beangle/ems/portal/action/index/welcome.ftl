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

[#list rowPortalets?keys?sort as rowIndex]
  [#assign sections = rowPortalets.get(rowIndex)]
  <div class="row content">
    [#list sections as section]
      <section class="col-lg-${section?first.colspan}">
        [#list section as portalet]
          [#if portalet.usingIframe]
          <iframe scrolling="auto" src="${portalet.url} id="portalet_${portalet.id}" width="100%" height="100%" frameborder="0"></iframe>
          [#else]
          <div id="portalet_${portalet.id}"></div>
          <script>bg.ready(function(){bg.Go('${portalet.url}','portalet_${portalet.id}')});</script>
          [/#if]
        [/#list]
      </section>
    [/#list]
  </div>
[/#list]

[@b.foot/]
