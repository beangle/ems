[#ftl]
[@b.head/]
<div class="search-container">
    <div class="search-panel">
    [@b.form name="cronJobLogSearchForm" action="!search" target="cronJobLoglist" title="ui.searchForm" theme="search"]
      [@b.select name="cronJobLog.job.id" label="计划任务" items=cronJobs option="id,name" empty="..."/]
      [@b.date name="beginOn" label="开始于"/]
      [@b.date name="endOn" label="~到"/]
      <input type="hidden" name="orderBy" value="cronJobLog.executeAt desc"/>
    [/@]
    </div>
    <div class="search-list">[@b.div id="cronJobLoglist" href="!search?orderBy=cronJobLog.executeAt desc"/]
  </div>
</div>
[@b.foot/]
