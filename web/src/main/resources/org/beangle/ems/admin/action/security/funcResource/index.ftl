[#ftl]
[@b.head/]
[#include "../func-nav.ftl"/]
[#include "scope.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="funcForm" action="!search" target="func_resource_list" title="ui.searchForm" theme="search"]
      [@b.select name="resource.app.id" label="应用" value=current_app items=apps option="id,fullTitle" style="width:100px"/]
      [@b.textfields names="resource.name;名称"/]
      [@b.field label="可见范围"]
      <select name="resource.scope" style="width:100px">
      <option value="">...</option>
        [#list scopes?keys as i]
        <option value="${i}">${scopes[i?string]}</option>
        [/#list]
      </select>
      [/@]
      <input type="hidden" name="orderBy" value="resource.name"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="func_resource_list" href="!search?resource.app.id=${current_app.id}&orderBy=name"/]
 </div>
</div>
[@b.foot/]
