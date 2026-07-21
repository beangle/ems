[#ftl]
[@b.head/]
[#include "../func-nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form action="!search?orderBy=menu.indexno" title="ui.searchForm" target="menulist" theme="search"]
      [@b.select name="menu.channel.app.id" value=current_app items=apps label="应用" option="id,fullTitle" required="true"/]
      [@b.select name="menu.channel.channelType.id" value=current_channelType items=channelTypes label="前端类型" option="id,title" required="true"/]
      [@b.textfields names="menu.indexno;common.code,menu.name;名称,menu.route;路由"/]
      [@b.select name="menu.enabled" label="common.status" items={'true':'${b.text("action.activate")}','false':'${b.text("action.freeze")}'}  empty="..."/]
    [/@]
 </div>
 <div class="search-list">
    [@b.div href="!search?menu.channel.app.id=${current_app.id}&menu.channel.channelType.id=${current_channelType.id}&orderBy=menu.indexno" id="menulist"/]
 </div>
</div>
[@b.foot/]
