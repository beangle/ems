[#ftl]
[@b.head/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="funcForm" action="!search" target="file_list" title="ui.searchForm" theme="search"]
      [@b.select name="file.app.id" label="应用" items=apps empty="..." option="id,fullTitle" style="width:100px"/]
      [@b.textfields names="file.name;文件路径"/]
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="file_list" href="!search?orderBy=file.app.title,file.name"/]
 </div>
</div>
[@b.foot/]
