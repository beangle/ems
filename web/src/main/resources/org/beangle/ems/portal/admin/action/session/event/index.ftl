[#ftl]
[@b.head/]
[#assign eventTypes={'0':'登录','1':'退出'} /]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="configSearchForm" action="!search" target="configlist" title="ui.searchForm" theme="search"]
      [@b.date name="beginOn" label="起始" value=beginOn/]
      [@b.date name="endOn" label="结束" value=endOn/]
      [@b.textfields names="sessionEvent.principal;用户,sessionEvent.username;姓名,sessionEvent.name;事件,sessionEvent.ip;IP"/]
      [@b.select name="sessionEvent.eventType" items=eventTypes label="类型" empty="..."/]
      <input type="hidden" name="orderBy" value="sessionEvent.updatedAt desc"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="configlist" href="!search?orderBy=sessionEvent.updatedAt desc&beginOn="+beginOn+"&endOn="+endOn/]
 </div>
</div>
[@b.foot/]
