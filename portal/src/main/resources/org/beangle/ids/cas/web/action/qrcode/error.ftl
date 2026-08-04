[@b.head title="授权失败"/]
<div class="container" style="max-width: 420px; margin-top: 80px;">
  <div class="card border-danger">
    <div class="card-header bg-danger text-white">操作失败</div>
    <div class="card-body">
      <p class="text-danger">${error!'二维码已失效，请重新扫码'}</p>
      [#if service??]
      <p class="text-muted small mb-0 mt-2">请求的服务地址：<code>${service}</code></p>
      [/#if]
    </div>
  </div>
</div>
[@b.foot/]
