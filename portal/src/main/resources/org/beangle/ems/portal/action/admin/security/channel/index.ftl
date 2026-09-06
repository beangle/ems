[#ftl]
[@b.head/]
[#include "../func-nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form action="!search" target="channellist" title="ui.searchForm" theme="search"]
      [@b.textfield name="channel.app.name" label="应用名称" /]
      [@b.textfield name="channel.app.title" label="应用标题" /]
      [@b.select name="channel.channelType.id" label="前端类型" items=channelTypes! option="id,title" empty="..."/]
      [@b.select name="embedMode.id" label="嵌入方式" items={'1':'微前端','2':'IFrame'} empty="..."/]
      [@b.select name="channel.enabled" label="common.status" items={'true':'${b.text("action.activate")}','false':'${b.text("action.freeze")}'} empty="..."/]
    [/@]
 </div>
 <div class="search-list">
    [@b.div href="!search" id="channellist"/]
 </div>
</div>
[@b.foot/]
