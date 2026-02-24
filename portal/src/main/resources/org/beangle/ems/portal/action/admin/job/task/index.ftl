[#ftl]
[@b.head/]
[#include "../job-nav.ftl"/]
<div class="search-container">
    <div class="search-panel">
    [@b.form name="taskSearchForm" action="!search" target="tasklist" title="ui.searchForm" theme="search"]
      [@b.textfields names="task.name;名称,task.target;目标"/]
      <input type="hidden" name="orderBy" value="task.updatedAt desc"/>
    [/@]
    </div>
    <div class="search-list">[@b.div id="tasklist" href="!search?orderBy=task.updatedAt desc"/]
  </div>
</div>
[@b.foot/]
