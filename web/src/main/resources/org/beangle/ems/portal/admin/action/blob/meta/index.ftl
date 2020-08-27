[#ftl]
[@b.head/]
[#include "../nav.ftl"/]
<div class="search-container">
 <div class="search-panel">
    [@b.form name="funcForm" action="!search" target="blobMeta_list" title="ui.searchForm" theme="search"]
      [@b.select name="blobMeta.profile.id" label="业务" items=profiles empty="..." option="id,name" style="width:100px"/]
      [@b.textfields names="blobMeta.name;文件名,blobMeta.owner;所有者,blobMeta.filePath;路径"/]
    [/@]
 </div>
 <div class="search-list">
   [@b.div id="blobMeta_list" href="!search?orderBy=blobMeta.updatedAt desc"/]
 </div>
</div>
[@b.foot/]
