[#ftl]
[@b.head/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="funcForm" action="!search" target="log_list" title="ui.searchForm" theme="search"]
      [@b.select name="businessLog.app.id" items=apps label="应用" option="id,fullTitle"/]
      [@b.textfields names="businessLog.operator;操作人,businessLog.summary;内容,businessLog.ip;IP"/]
      [@b.date name="beginOn" label="开始于"/]
      [@b.date name="endOn" label="~到"/]
      [@b.select name="businessLog.logLevel" items=levels label="级别"/]
      <input name="orderBy" value="businessLog.operateAt desc" type="hidden"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="log_list" href="!search?orderBy=businessLog.operateAt desc"/]
 </div>
</div>
[@b.foot/]
