[#ftl]
[@b.head/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="funcForm" action="!search" target="template_list" title="ui.searchForm" theme="search"]
      [@b.select name="template.app.id" label="应用" items=apps empty="..." option="id,name" style="width:100px"/]
      [@b.textfields names="template.name;文件路径"/]
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="template_list" href="!search?orderBy=template.name"/]
 </div>
</div>
[@b.foot/]
