[#ftl]
[@b.head/]
[#include "../func-nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form action="!search?orderBy=menu.indexno" title="ui.searchForm" target="menulist" theme="search"]
      [@b.select name="menu.app.id" value=current_app items=apps label="应用" option="id,fullTitle"/]
      [@b.textfields names="menu.indexno;common.code,menu.title;标题,menu.entry.name;menu.entry"/]
      [@b.select name="menu.enabled" items=profiles label="common.status" items={'true':'${b.text("action.activate")}','false':'${b.text("action.freeze")}'}  empty="..."/]
    [/@]
 </div>
 <div class="search-list">
    [@b.div  href="!search?menu.app.id=${current_app.id}&orderBy=menu.indexno" id="menulist"/]
 </div>
</div>
[@b.foot/]
