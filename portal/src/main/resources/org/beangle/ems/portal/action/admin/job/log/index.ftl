[#ftl]
[@b.head/]
[#include "../job-nav.ftl"/]
<div class="search-container">
    <div class="search-panel">
    [@b.form name="logSearchForm" action="!search" target="loglist" title="ui.searchForm" theme="search"]
      [@b.select name="log.task.id" label="计划任务" items=cronTasks option="id,name" empty="..."/]
      [@b.date name="beginOn" label="开始于"/]
      [@b.date name="endOn" label="~到"/]
      <input type="hidden" name="orderBy" value="log.executeAt desc"/>
    [/@]
    </div>
    <div class="search-list">[@b.div id="loglist" href="!search?orderBy=log.executeAt desc"/]
  </div>
</div>
[@b.foot/]
