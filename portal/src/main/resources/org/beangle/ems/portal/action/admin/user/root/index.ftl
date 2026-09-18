[#ftl]
[@b.head/]
[#include "../user-nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
  [@b.form name="searchForm" action="!search" title="ui.searchForm" target="rootList" theme="search"]
    [@b.textfield label="账户" name="root.user.code"/]
    [@b.select label="是否有效" name="active" items={"1":"是","0":"否"} empty="..."/]
  [/@]
 </div>
 <div class="search-list">
   [@b.div id="rootList" href="!search" /]
 </div>
</div>
[@b.foot/]
