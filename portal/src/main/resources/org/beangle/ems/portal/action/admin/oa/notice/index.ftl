[#ftl]
[@b.head/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="noticeSearchForm" action="!search" target="noticelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="notice.title;标题"/]
      [@b.select label="用户类别" name="category.id" items=categories empty="..."/]
      [@b.select label="应用" name="notice.app.id" items=apps option="id,title" empty="..."/]
      [@b.select label="状态" name="notice.status" items={"0":"草稿","1":"提交","2":"审核不通过","3":"审核通过"}  empty="..."/]
      [@b.textfield label="起草人" name="notice.operator.name" /]
      [@b.select label="是否归档" name="notice.archived" items={"1":"是","0":"否"} empty="..."/]
      <input type="hidden" name="orderBy" value="notice.updatedAt desc"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="noticelist" href="!search?orderBy=notice.updatedAt desc"/]
 </div>
</div>
[@b.foot/]
