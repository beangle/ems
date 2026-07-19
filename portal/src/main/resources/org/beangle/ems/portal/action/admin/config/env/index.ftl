[#ftl]
[@b.head/]
[#include "../app/nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="envSearchForm" action="!search" target="envlist" title="ui.searchForm" theme="search"]
      [@b.textfields names="env.code;代码,env.name;名称"/]
      <input type="hidden" name="orderBy" value="env.code"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="envlist" href="!search?orderBy=env.code"/]
 </div>
</div>
[@b.foot/]
