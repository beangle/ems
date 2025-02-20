[#ftl]
[@b.head/]
[#include "../flow-nav.ftl"/]
<div class="search-container">
    <div class="search-panel">
    [@b.form name="flowSearchForm" action="!search" target="flowlist" title="ui.searchForm" theme="search"]
      [@b.select name="flow.business.id" label="业务类型" items=businesses/]
      [@b.textfields names="flow.name;名称"/]
      <input type="hidden" name="orderBy" value="flow.business.id,flow.name"/>
    [/@]
    </div>
    <div class="search-list">[@b.div id="flowlist" href="!search?orderBy=flow.name"/]
  </div>
</div>
[@b.foot/]
