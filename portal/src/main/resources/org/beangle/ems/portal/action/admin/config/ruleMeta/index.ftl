[#ftl]
[@b.head/]
[#include "../nav_rule.ftl"/]
<div class="search-container">
    <div class="search-panel">
    [@b.form name="ruleMetaSearchForm" action="!search" target="ruleMetalist" title="ui.searchForm" theme="search"]
      [@b.textfields names="ruleMeta.name;名称"/]
      <input type="hidden" name="orderBy" value="ruleMeta.name"/>
    [/@]
    </div>
    <div class="search-list">[@b.div id="ruleMetalist" href="!search?orderBy=ruleMeta.name"/]
  </div>
</div>
[@b.foot/]
