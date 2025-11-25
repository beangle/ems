[#ftl]
[@b.head/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="funcForm" action="!search" target="log_list" title="ui.searchForm" theme="search"]
      [@b.select name="errorLog.app.id" items=apps label="应用" option="id,fullTitle"/]
      [@b.textfields names="errorLog.username;操作人,errorLog.message;消息,errorLog.exceptionName;异常名称"/]
      [@b.date name="beginOn" label="开始于"/]
      [@b.date name="endOn" label="~到"/]
      <input name="orderBy" value="errorLog.occurredAt desc" type="hidden"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="log_list" href="!search?orderBy=errorLog.occurredAt desc"/]
 </div>
</div>
[@b.foot/]
