[#ftl]
[@b.head/]
[#include "../flow-nav.ftl"/]
<div class="search-container">
    <div class="search-panel">
    [@b.form name="templateSearchForm" action="!search" target="templatelist" title="ui.searchForm" theme="search"]
      [@b.select name="template.business.id" label="业务类型" items=businesses /]
      [@b.textfields names="template.name;名称"/]
      <input type="hidden" name="orderBy" value="template.business.id,template.name"/>
    [/@]
    </div>
    <div class="search-list">[@b.div id="templatelist" href="!search?orderBy=template.name"/]
  </div>
</div>
[@b.foot/]
