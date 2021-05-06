[#ftl]
[@b.head/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="funcForm" action="!search" target="reconfig_list" title="ui.searchForm" theme="search"]
      [@b.select name="reconfig.app.id" label="应用" items=apps empty="..." option="id,name" style="width:100px"/]
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="reconfig_list" href="!search?orderBy=reconfig.app.name"/]
 </div>
</div>
[@b.foot/]
