[#ftl]
[@b.head/]
<div class="search-container">
 <div class="search-panel">
  [@b.form name="searchForm" action="!search" title="ui.searchForm" target="dimensionList" theme="search"]
    [@b.textfields names="dimension.name,dimension.title"/]
  [/@]
 </div>
 <div class="search-list">
  [@b.div id="dimensionList" href="!search" /]
 </div>
</div>
[@b.foot/]
