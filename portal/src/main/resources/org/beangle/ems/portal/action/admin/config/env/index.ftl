[#ftl]
[@b.head/]
[#include "../app/nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="envSearchForm" action="!search" target="envlist" title="ui.searchForm" theme="search"]
      [@b.textfields names="env.name;名称"/]
      <input type="hidden" name="orderBy" value="env.name"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="envlist" href="!search?orderBy=env.name"/]
 </div>
</div>
[@b.foot/]
