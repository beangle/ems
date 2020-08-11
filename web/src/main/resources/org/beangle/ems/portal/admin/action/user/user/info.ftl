[#ftl]
[@b.head/]
[@b.toolbar title='用户明细']bar.addBack("${b.text("action.back")}");[/@]
[#macro info(name,title='')]
  [#if title=='']
   <td class="title">${b.text('user.'+name)}:</td>
  [#else]
  <td class="title">${b.text(title)}:</td>
  [/#if]
   <td class="content"> ${(user[name]?string)!}</td>
[/#macro]

<table class="infoTable">
  <tr>
   [@info 'code' /]
   [@info 'name' /]
  </tr>
  <tr>
    <td class="title">状态:</td>
    <td class="content">[#if user.enabled] ${b.text("action.activate")}[#else]${b.text("action.freeze")}[/#if]</td>
    <td class="title">是否锁定:</td>
    <td class="content">[#if user.locked]锁定[#else]正常[/#if]</td>
  </tr>
  <tr>
    <td class="title" >${b.text("user.members")}:</td>
    <td class="content" colspan="3">[#list user.roles?sort_by(['role','indexno']) as m]${m.role.indexno} ${m.role.name}([#if m.member]成员[/#if][#if m.manager] 管理者[/#if][#if m.granter] 可授权[/#if])<br>[/#list]</td>
  </tr>
  <tr>
    <td class="title">身份:</td>
    <td class="content">${user.category.name}</td>
    <td class="title">有效期:</td>
    <td class="content">${user.beginOn!}~${user.endOn!}</td>
  </tr>
  <tr>
    <td class="title" >密码有效期:</td>
    <td class="content">
      [#if credential??]
        ${credential.updatedAt?string("yyyy-MM-dd HH:mm:ss")}～${credential.expiredOn}
      [/#if]
    </td>
    [@info 'updatedAt','common.updatedAt' /]
  </tr>
  <tr>
  <td class="title" >${b.text("common.remark")}:</td>
  <td class="content">${user.remark!}</td>
  </tr>
</table>
[@b.foot/]
