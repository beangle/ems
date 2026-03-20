[@b.head title="应用授权"/]
<div class="container" style="max-width: 500px; margin-top: 80px;">
  <div class="card">
    <div class="card-header">
      <h5 class="mb-0">${app.name!''} 请求访问您的账户</h5>
    </div>
    <div class="card-body">
      <p class="text-muted">${app.name!''} 希望获取以下权限：</p>
      <form action="${b.url('!approve')}" method="post">
        <input type="hidden" name="client_id" value="${clientId!''}"/>
        <input type="hidden" name="redirect_uri" value="${redirectUri!''}"/>
        <input type="hidden" name="response_type" value="${responseType!'code'}"/>
        <input type="hidden" name="state" value="${state!''}"/>
        <input type="hidden" name="code_challenge" value="${codeChallenge!''}"/>
        <input type="hidden" name="code_challenge_method" value="${codeChallengeMethod!'S256'}"/>
        <div class="mb-3">
          <label class="form-label">授权范围（选择要授予的角色）</label>
          [#if roles?? && roles?size > 0]
          <div class="list-group">
            [#list roles as role]
            <label class="list-group-item list-group-item-action">
              <input class="form-check-input me-2" type="checkbox" name="scope" value="${role.id}"/>
              ${role.name!''}
            </label>
            [/#list]
          </div>
          [#else]
          <p class="text-muted small">您暂无可用角色</p>
          [/#if]
        </div>
        <div class="d-flex justify-content-between">
          <button type="submit" name="approved" value="false" class="btn btn-outline-secondary">拒绝</button>
          <button type="submit" name="approved" value="true" class="btn btn-primary">授权</button>
        </div>
      </form>
    </div>
  </div>
</div>
[@b.foot/]
