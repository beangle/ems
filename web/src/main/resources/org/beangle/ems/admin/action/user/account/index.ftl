[#ftl]
[@b.head/]
[#include "../user-nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
  [@b.form name="userSearchForm" action="!search" title="ui.searchForm" target="accountlist" theme="search"]
    [@b.textfields names="account.user.code;账户,account.user.name;姓名,roleName;角色"/]
    [@b.select items=categories label="身份" name="account.user.category.id" empty="..."/]
    [@b.select name="account.enabled" label="common.status" value="1" empty="..." items={'1':'${b.text("action.activate")}','0':'${b.text("action.freeze")}'}/]
  [/@]
 </div>
 <div class="search-list">
  [@b.div id="accountlist" href="!search?account.enabled=1" /]
 </div>
</div>
[@b.foot/]
