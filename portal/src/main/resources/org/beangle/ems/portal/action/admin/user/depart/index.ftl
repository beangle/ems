[#ftl]
[@b.head/]
[#include "../user-nav.ftl"/]
<div class="search-container">
    <div class="search-panel">
    [@b.form name="departSearchForm" action="!search" target="departlist" title="ui.searchForm" theme="search"]
      [@b.textfields names="depart.code;代码"/]
      [@b.textfields names="depart.name;名称"/]
      [@b.textfields names="depart.indexno;序号"/]
      [@b.select label="是否有效"  name="active" items={"1":"是","0":"否"} value="1" empty="..."/]
      <input type="hidden" name="orderBy" value="depart.indexno"/>
    [/@]
    </div>
    <div class="search-list">
      [@b.div id="departlist" href="!search?orderBy=depart.indexno&active=1"/]
    </div>
  </div>
[@b.foot/]
