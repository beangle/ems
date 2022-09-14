[#ftl]
[@b.head/]
[#include "nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="dataSourceAppSearchForm" action="!search" target="dataSourceApplist" title="ui.searchForm" theme="search"]
      [@b.textfields names="app.name;名称,app.title;标题"/]
      [@b.select name="app.appType.id" label="类型" items=appTypes empty="..."/]
      [@b.select name="app.group.id" label="分组" items=groups option="id,title" empty="..."/]
      <input type="hidden" name="orderBy" value="app.group.indexno,app.indexno"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="dataSourceApplist" href="!search?orderBy=app.group.indexno,app.indexno"/]
 </div>
</div>
[@b.foot/]
