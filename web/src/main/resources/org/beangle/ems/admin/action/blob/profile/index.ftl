[#ftl]
[@b.head/]
[#include "../nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="funcForm" action="!search" target="profile_list" title="ui.searchForm" theme="search"]
      [@b.textfields names="profile.path;路径"/]
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="profile_list" href="!search?orderBy=profile.path"/]
 </div>
</div>
[@b.foot/]
