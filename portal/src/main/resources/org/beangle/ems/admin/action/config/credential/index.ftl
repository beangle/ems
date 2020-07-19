[#ftl]
[@b.head/]
[#include "../nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="credentialSearchForm" action="!search" target="credentiallist" title="ui.searchForm" theme="search"]
      [@b.textfields names="credential.name;名称"/]
      [@b.textfields names="credential.username;用户名"/]
      <input type="hidden" name="orderBy" value="name"/>
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="credentiallist" href="!search?orderBy=name"/]
 </div>
</div>
[@b.foot/]
