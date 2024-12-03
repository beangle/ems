[#ftl]
[@b.head/]
[#include "../nav_rule.ftl"/]
<div class="search-container">
    <div class="search-panel">
    [@b.form name="ruleSearchForm" action="!search" target="rulelist" title="ui.searchForm" theme="search"]
      [@b.select name="rule.meta.business.id" label="业务类型" items=businesses/]
      [@b.textfields names="rule.name;名称"/]

      <input type="hidden" name="orderBy" value="rule.meta.business.id,rule.name"/>
    [/@]
    </div>
    <div class="search-list">[@b.div id="rulelist" href="!search?orderBy=rule.name"/]
  </div>
</div>
[@b.foot/]
