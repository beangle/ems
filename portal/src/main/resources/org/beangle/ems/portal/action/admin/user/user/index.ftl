[#ftl]
[@b.head/]
[#include "../user-nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
  [@b.form name="userSearchForm" action="!search" title="ui.searchForm" target="accountlist" theme="search"]
    [@b.textfields names="user.code;账户,user.name;姓名,roleName;角色,groupName;用户组"/]
    [@b.select items=categories label="身份" name="user.category.id" empty="..."/]
    [@b.select items=departs label="部门" name="user.depart.id" empty="..."/]
    [@b.select name="user.enabled" label="common.status" value="1" empty="..." items={'1':'${b.text("action.activate")}','0':'${b.text("action.freeze")}'}/]
    [@b.select label="照片" name="hasAvatar" items={"1":"已上传","0":"无"} empty="..."/]
  [/@]
 </div>
 <div class="search-list">
  [@b.div id="accountlist" href="!search?user.enabled=1" /]
 </div>
</div>
[@b.foot/]
