[#ftl]
[@b.head/]
[#include "../user-nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
  [@b.form name="userSearchForm" action="!search" title="ui.searchForm" target="userlist" theme="search"]
    [@b.textfields names="user.code,user.name,roleName;user.roles"/]
    [@b.select items=categories label="user.category.name" name="user.category.id" empty="..."/]
    [@b.select name="user.enabled" label="common.status" value="1" empty="..." items={'1':'${b.text("action.activate")}','0':'${b.text("action.freeze")}'}/]
  [/@]
 </div>
 <div class="search-list">
  [@b.div id="userlist" href="!search?user.enabled=1" /]
 </div>
</div>
[@b.foot/]
