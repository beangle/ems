[#ftl]
[@b.head/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="funcForm" action="!search" target="log_list" title="ui.searchForm" theme="search"]
      [@b.textfields names="businessLog.operator;操作人,businessLog.summary;内容,businessLog.app.name;应用"/]
      <input name="orderBy" value="businessLog.operateAt desc" type="hidden"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="log_list" href="!search?orderBy=businessLog.operateAt desc"/]
 </div>
</div>
[@b.foot/]
