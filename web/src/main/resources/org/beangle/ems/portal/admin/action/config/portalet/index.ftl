[#ftl]
[@b.head/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="portaletSearchForm" action="!search" target="portaletlist" title="ui.searchForm" theme="search"]
      [@b.textfields names="portalet.name;名称,portalet.title;标题"/]
      <input type="hidden" name="orderBy" value="portalet.idx"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="portaletlist" href="!search?orderBy=portalet.idx"/]
 </div>
</div>
[@b.foot/]
