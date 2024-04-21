[#ftl]
<div class="card card-primary card-outline">
  <div class="card-header"><h3 class="card-title">应用和菜单权限</h3></div>
  <div class="card-body">

    [#list menus.groups as groupMenu]
      <p style="border-bottom: 1px solid rgba(0,0,0,.125);"><strong><i class="fa-solid fa-list mr-1"></i>${groupMenu.group['title']}</strong></p>
      [#list groupMenu.appMenus as appMenu]
      <strong><i class="fa-regular fa-flag mr-1"></i>${appMenu.app['title']}</strong>
      <p class="text-muted">[#list appMenu.menus as menu][@displayMenu menu/][/#list]</p>
      [/#list]
[/#list]

  </div>
</div>

[#macro displayMenu menu]
${menu['title']}&nbsp;
  [#if menu['children']??]
   [#list menu['children'] as c]
   [@displayMenu c/]
   [/#list]
  [/#if]
[/#macro]
