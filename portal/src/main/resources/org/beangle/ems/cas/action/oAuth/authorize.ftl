[@b.head title="应用授权"/]
<div class="container" style="max-width: 500px; margin-top: 80px;">
  <div class="card">
    <div class="card-header">
      <h5 class="mb-0">${app.name!''} 请求访问您的账户(${user.name})</h5>
    </div>
    <div class="card-body">
      <p class="text-muted">${app.name!''} 希望获取以下权限：</p>
      [#if tokenTTL??]
      <p class="form-text text-muted small mb-3">
        <span class="text-secondary">提示：</span>同意授权后，第三方应用换取的访问令牌有效期为
        [#assign sec = tokenTTL.seconds]
        [#if sec > (3600 -1)]
          约 <strong>${(sec / 3600)?floor}</strong> 小时（${sec?c} 秒）
        [#elseif sec > (60-1)]
          约 <strong>${(sec / 60)?floor}</strong> 分钟（${sec?c} 秒）
        [#else]
          <strong>${sec?c}</strong> 秒
        [/#if]
        。过期后需重新授权。
      </p>
      [/#if]
      [@b.form action="!approve" ]
        <input type="hidden" name="client_id" value="${clientId!''}"/>
        <input type="hidden" name="redirect_uri" value="${redirectUri!''}"/>
        <input type="hidden" name="state" value="${Parameters['state']!}"/>
        <input type="hidden" name="code_challenge" value="${Parameters['code_challenge']!}"/>
        <div class="mb-3">
          <label class="form-label">授权范围（选择要授予的角色）</label>
          [#if roles?? && roles?size > 0]
          <style>
            /* 仅清爽本页面角色列表：去掉 list-group-item 的边框 */
            .cas-oauth-role-list .list-group-item{
              border:0 !important;
              background:transparent;
              padding: .15rem 1.25rem;
              margin: 0;
            }
          </style>
          <div class="list-group cas-oauth-role-list">
            <div class="d-flex gap-2 mb-2">
              <button type="button" class="btn btn-sm btn-outline-primary" onclick="casSelectAllScopes(true)">全选</button>
              <button type="button" class="btn btn-sm btn-outline-secondary" onclick="casSelectAllScopes(false)">全不选</button>
            </div>
            [#list roles as role]
            <label class="list-group-item list-group-item-action">
              <input class="form-check-input" type="checkbox" name="scope" value="${role.id}"/>
              ${role.name!''}
            </label>
            [/#list]
          </div>
          <script>
            function casOauthUpdateApproveButton() {
              var btn = document.getElementById('casOauthApproveBtn');
              if (!btn) return;
              var boxes = document.querySelectorAll('.cas-oauth-role-list input[name="scope"]');
              if (!boxes.length) {
                btn.disabled = true;
                return;
              }
              var any = false;
              for (var i = 0; i < boxes.length; i++) {
                if (boxes[i].checked) { any = true; break; }
              }
              btn.disabled = !any;
            }
            function casSelectAllScopes(checked) {
              document.querySelectorAll('.cas-oauth-role-list input[name="scope"]').forEach(function (el) {
                el.checked = checked;
              });
              casOauthUpdateApproveButton();
            }
            document.addEventListener('DOMContentLoaded', function () {
              var list = document.querySelector('.cas-oauth-role-list');
              if (list) {
                list.addEventListener('change', function (e) {
                  if (e.target && e.target.name === 'scope') casOauthUpdateApproveButton();
                });
                casOauthUpdateApproveButton();
              }
            });
          </script>
          [#else]
          <p class="text-muted small">您暂无可用角色</p>
          [/#if]
        </div>
        <div class="d-flex justify-content-between">
          <button type="submit" name="approved" value="false" class="btn btn-outline-secondary">拒绝</button>
          <button type="submit" name="approved" value="true" class="btn btn-primary" id="casOauthApproveBtn" disabled>授权</button>
        </div>
      [/@]
    </div>
  </div>
</div>
[@b.foot/]
