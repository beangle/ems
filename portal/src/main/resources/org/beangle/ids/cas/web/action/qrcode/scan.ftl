[@b.head title="扫码确认登录"/]
<div class="container" style="max-width: 420px; margin-top: 80px;">
  <div class="card">
    <div class="card-header">
      <h5 class="mb-0">确认登录</h5>
    </div>
    <div class="card-body">
      <p class="text-muted">${app.name} 请求登录 EMS</p>
      <p class="mb-3">当前用户：<strong>${user.name!user.code!''}</strong></p>
      <div class="d-flex justify-content-between">
        <form method="post" action="${b.base}/cas/qrcode/cancel">
          <input type="hidden" name="qrcodeId" value="${qrcodeId}"/>
          <button type="submit" class="btn btn-outline-secondary">拒绝</button>
        </form>
        <form method="post" action="${b.base}/cas/qrcode/confirm">
          <input type="hidden" name="qrcodeId" value="${qrcodeId}"/>
          <button type="submit" name="approved" value="true" class="btn btn-primary">确认登录</button>
        </form>
      </div>
    </div>
  </div>
</div>
[@b.foot/]
