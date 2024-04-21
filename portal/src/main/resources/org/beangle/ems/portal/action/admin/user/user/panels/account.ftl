  <div class="card card-primary card-outline">
    <div class="card-body box-profile">
      <div class="text-center">
        <img class="profile-user-img img-fluid img-circle" src="${avatar_url}" alt="User profile picture">
      </div>
      <h3 class="profile-username text-center">${user.name}</h3>
      <p class="text-muted text-center">${user.code}</p>
      <ul class="list-group list-group-unbordered mb-3">
        <li class="list-group-item">
         [@b.a href="!edit?user.id=${user.id}" target="user-info" ]修改账户[/@]
         [#if isRoot]<a href="${portal_url}" onclick="runAs('${user.code}')" class="float-right">模拟登录</a>[/#if]
        </li>
      </ul>
      <strong><i class="fa-solid fa-phone mr-1"></i> 联系方式</strong>
      <p class="text-muted">
        [#if user.mobile?? && user.mobile?length>10]
          <span title="${user.mobile}">${(user.mobile[0..2])!}****${(user.mobile[7..10])!}</span>
        [#else]
          ${(user.mobile)!"--"}
        [/#if]
        ${user.email!}
      </p>
      <strong><i class="fa-solid fa-wifi mr-1"></i> 状态</strong>
      <p class="text-muted">
        [#if user.locked] ${b.text("action.freeze")}[#else]${b.text("action.activate")}[/#if]
         ${(user.beginOn)!}～${(user.endOn)!}
      </p>
      <strong><i class="fa-solid fa-key mr-1"></i> 密码过期日期</strong>
      <p class="text-muted">
        [#if user.passwdExpiredOn??]${(user.passwdExpiredOn)!}[#else]永不过期[/#if]
      </p>

    [#assign members=[]/]
    [#assign granters=[]/]
    [#assign managers=[]/]
    [#list user.roles as rm]
      [#if rm.member][#assign members=members+[rm.role]/][/#if]
      [#if rm.granter][#assign granters=granters+[rm.role]/][/#if]
      [#if rm.manager][#assign managers=managers+[rm.role]/][/#if]
    [/#list]
    [#if members?size>0]
      <strong><i class="fas fa-user mr-1"></i> 持有角色</strong>
      <p class="text-muted">
        [#list members as r]${r.name}&nbsp;[/#list]
      </p>
    [/#if]

    [#if granters?size>0]
      <strong><i class="fa-solid fa-shield mr-1"></i> 可授权</strong>
      <p class="text-muted">
        [#list granters as r]${r.name}&nbsp;[/#list]
      </p>
    [/#if]

    [#if managers?size>0]
      <strong><i class="fas fa-user mr-1"></i> 可管理</strong>
      <p class="text-muted">
        [#list managers as r]${r.name}&nbsp;[/#list]
      </p>
    [/#if]
    </div>
  </div>
  <script>
    function runAs(u){
      beangle.cookie.set('beangle.security.runAs',u,"/",1)
      return true;
    }
  </script>
