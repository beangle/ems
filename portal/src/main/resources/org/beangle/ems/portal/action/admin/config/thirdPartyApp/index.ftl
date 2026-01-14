[#ftl]
[@b.head/]
[#include "../app/nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="dataSourceAppSearchForm" action="!search" target="dataSourceApplist" title="ui.searchForm" theme="search"]
      [@b.textfields names="app.name;名称,app.title;标题"/]
      <input type="hidden" name="orderBy" value="app.code"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="dataSourceApplist" href="!search?orderBy=app.code"/]
 </div>
</div>
[@b.foot/]
