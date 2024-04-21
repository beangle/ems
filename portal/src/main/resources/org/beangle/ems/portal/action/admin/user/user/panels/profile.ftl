[#ftl]
<div class="card card-primary card-outline">
  <div class="card-header"><h3 class="card-title">数据权限范围</h3></div>
  <div class="card-body">
  [#if (profiles?size==0)]没有设置[/#if]
   [#list profiles as profile]
    <p style="border-bottom: 1px solid rgba(0,0,0,.125);"><strong>数据权限--${profile.name}</strong></p>
    [#list profile.properties?keys as field]
    <strong>${field.title}</strong>
    <p class="text-muted">
      [#assign values=fieldMaps[profile.id?string][field.name]/]
      [#if values?is_string]
        ${values}
      [#else]
      [#list values as v][#list field.properties?split(",") as pName]${v[pName]!} [/#list][#if v_has_next],[/#if][/#list]
      [/#if]
    </p>
    [/#list]
  [/#list]
  </div>
</div>
