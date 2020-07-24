[#ftl]
[@b.head/]
[#include "../user-nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="roleSearchForm"  action="!search" target="rolelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="role.name;common.name,role.creator.name;common.creator"/]
    [/@]
 </div>
 <div class="search-list">
  [@b.div id="rolelist" href="!search?orderBy=role.indexno" /]
 </div>
</div>
[@b.foot/]
