[#ftl]
[@b.head/]
[#include "../nav_theme.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="funcForm" action="!search" target="file_list" title="ui.searchForm" theme="search"]
      [@b.select name="file.app.id" label="应用" items=apps empty="..." option="id,fullTitle" style="width:100px"/]
      [@b.textfields names="bundle.name;名称"/]
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="file_list" href="!search?orderBy=bundle.name"/]
 </div>
</div>
[@b.foot/]
