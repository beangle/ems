[#ftl]
[@b.head/]
[#include "../flow-nav.ftl"/]
<div class="search-container">
    <div class="search-panel">
    [@b.form name="flowSearchForm" action="!search" target="processList" title="ui.searchForm" theme="search"]
      [@b.select name="process.flow.business.id" label="业务类型" items=businesses/]
      [@b.textfields names="process.flow.name;名称"/]
      [@b.textfield name="initiator" label="发起人" placeholder="账户或姓名"/]
      [@b.textfield name="process.businessKey" label="业务key"/]

      <input type="hidden" name="orderBy" value="process.startAt desc"/>
    [/@]
    </div>
    <div class="search-list">[@b.div id="processList" href="!search?orderBy=process.startAt desc"/]
  </div>
</div>
[@b.foot/]
