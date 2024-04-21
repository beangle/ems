[#ftl]
[@b.head/]
[#include "../user-nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
  [@b.form name="searchForm" action="!search" title="ui.searchForm" target="rootList" theme="search"]
    [@b.textfield label="账户" name="root.user.code"/]
    [@b.textfield label="应用名称" name="root.app.name"/]
  [/@]
 </div>
 <div class="search-list">
   [@b.div id="rootList" href="!search" /]
 </div>
</div>
[@b.foot/]
