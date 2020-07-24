[#ftl]
[@b.head/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="configSearchForm" action="!search" target="configlist" title="ui.searchForm" theme="search"]
      [@b.textfields names="sessionConfig.category.name;用户分类"/]
      <input type="hidden" name="orderBy" value="sessionConfig.category.name"/>
    [/@]
 </div>
 <div class="search-list">
      [@b.div id="configlist" href="!search?orderBy=sessionConfig.category.name"/]
 </div>
</div>
[@b.foot/]
