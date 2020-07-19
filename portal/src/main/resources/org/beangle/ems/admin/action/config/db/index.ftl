[#ftl]
[@b.head/]
[#include "../nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="dbSearchForm" action="!search" target="dblist" title="ui.searchForm" theme="search"]
      [@b.textfields names="db.name;名称"/]
      [@b.textfields names="db.url;地址"/]
      <input type="hidden" name="orderBy" value="name"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="dblist" href="!search?orderBy=name"/]
 </div>
</div>
[@b.foot/]
