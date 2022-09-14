[#ftl]
[@b.head/]
[#include "../app/nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="groupSearchForm" action="!search" target="grouplist" title="ui.searchForm" theme="search"]
      [@b.textfields names="appGroup.name;名称,appGroup.title;标题"/]
      <input type="hidden" name="orderBy" value="appGroup.indexno"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="grouplist" href="!search?orderBy=appGroup.indexno"/]
 </div>
</div>
[@b.foot/]
