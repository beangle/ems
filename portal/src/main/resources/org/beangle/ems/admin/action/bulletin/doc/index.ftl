[#ftl]
[@b.head/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="docSearchForm" action="!search" target="doclist" title="ui.searchForm" theme="search"]
    <input type="hidden" name="orderBy" value="doc.updatedAt desc"/>
      [@b.textfields names="doc.name;标题"/]
      [@b.select label="用户类别" name="userCategory.id" items=userCategories empty="..."/]
      [@b.select label="是否归档" name="doc.archived" items={"1":"是","0":"否"} empty="..."/]
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="doclist" href="!search?orderBy=doc.updatedAt desc"/]
 </div>
</div>
[@b.foot/]
