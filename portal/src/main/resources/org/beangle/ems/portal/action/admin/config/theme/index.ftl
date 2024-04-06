[#ftl]
[@b.head/]
[#include "../nav_theme.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="themeSearchForm" action="!search" target="themelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="theme.name;名称"/]
      <input type="hidden" name="orderBy" value="name"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="themelist" href="!search?orderBy=name"/]
 </div>
</div>
[@b.foot/]
