[#ftl]
[@b.head/]
[#include "../user-nav.ftl"/]

<div class="search-container">
    <div class="search-panel">
    [@b.form name="groupSearchForm" action="!search" target="grouplist" title="ui.searchForm" theme="search"]
      [@b.textfield name="group.code" label="代码" /]
      [@b.textfield name="group.name" label="名称"/]
      <input type="hidden" name="orderBy" value="group.indexno"/>
    [/@]
    </div>
    <div class="search-list">
      [@b.div id="grouplist" href="!search?orderBy=group.indexno"/]
    </div>
  </div>
[@b.foot/]
