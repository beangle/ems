[#ftl]
[@b.head/]
[@b.toolbar title='info.role']
bar.addBack("${b.text("action.back")}");
[/@]
<table class="table table-sm table-detail">
  <tr>
   <td class="title" width="10%">${b.text("common.name")}:</td>
   <td  width="40%"> ${role.name}</td>
   <td class="title" width="10%">${b.text("common.creator")}:</td>
   <td  width="40%">${(role.creator.name)!}  </td>
  </tr>
  <tr>
   <td class="title" >修改时间:</td>
   <td >${role.updatedAt}</td>
   <td class="title">业务场景:</td>
   <td>[#if role.envs?size>0][#list role.envs as env]${env.code} ${env.name}[#sep]、[/#list][#else]不限[/#if]</td>
  </tr>
  <tr>
  <td class="title" >&nbsp;${b.text("common.status")}:</td>
  <td  colspan="3">
    [#if role.enabled] ${b.text("action.activate")}
    [#else]${b.text("action.freeze")}
    [/#if]
  </td>
  </tr>
  <tr>
  <td class="title" >${b.text("common.remark")}:</td>
  <td   colspan="3">${role.remark!}</td>
  </tr>
  <tr>
    <td class="title">应用场景授权:</td>
    <td colspan="3">
      [#if roleAppEnvMap?? && roleAppEnvMap?size>0]
      <table class="table table-sm table-bordered" style="margin:0;width:auto;min-width:60%;">
        <thead>
          <tr>
            <th>应用</th>
            <th>限定场景</th>
          </tr>
        </thead>
        <tbody>
          [#list roleAppEnvMap as app, envs]
          <tr>
            <td>${app.indexno!} ${app.title} <span class="text-muted">(${app.name})</span></td>
            <td>[#list envs as env]${env.code} ${env.name}[#sep]、[/#list]</td>
          </tr>
          [/#list]
        </tbody>
      </table>
      <div class="text-muted" style="margin-top:4px;">未列出的已授权应用表示不限定场景（全部场景）。</div>
      [#else]
      --（无应用级场景限定，或尚未授权功能）
      [/#if]
    </td>
  </tr>
</table>
[@b.foot/]
