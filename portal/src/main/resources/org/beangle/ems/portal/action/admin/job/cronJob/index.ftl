[#ftl]
[@b.head/]
<div class="search-container">
    <div class="search-panel">
    [@b.form name="cronJobSearchForm" action="!search" target="cronJoblist" title="ui.searchForm" theme="search"]
      [@b.textfields names="cronJob.name;名称,cronJob.target;目标"/]
      <input type="hidden" name="orderBy" value="cronJob.updatedAt desc"/>
    [/@]
    </div>
    <div class="search-list">[@b.div id="cronJoblist" href="!search?orderBy=cronJob.updatedAt desc"/]
  </div>
</div>
[@b.foot/]
