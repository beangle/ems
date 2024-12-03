[#ftl]
[@b.head/]
[#include "../nav_rule.ftl"/]
<div class="search-container">
    <div class="search-panel">
    [@b.form name="businessSearchForm" action="!search" target="businesslist" title="ui.searchForm" theme="search"]
      [@b.textfields names="business.name;名称"/]
      <input type="hidden" name="orderBy" value="business.code"/>
    [/@]
    </div>
    <div class="search-list">[@b.div id="businesslist" href="!search?orderBy=business.code"/]
  </div>
</div>
[@b.foot/]
