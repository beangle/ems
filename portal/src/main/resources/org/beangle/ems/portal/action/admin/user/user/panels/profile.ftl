[#ftl]
<div class="card card-primary card-outline">
  <div class="card-header"><h3 class="card-title">数据权限范围</h3></div>
  <div class="card-body" style="padding-top:0px;">
  [#if (profiles?size==0)]没有设置[/#if]
   [#list profiles as profile]
    <p style="border-bottom: 1px solid rgba(0,0,0,.125);margin-top:1rem;margin-bottom:0px;"><strong>数据权限--${profile.env.name}</strong></p>
    [#list profile.properties?keys as fieldName]
    [#assign field = dimensionMap[fieldName] /]
    <span>${field.title}</span>
    <span class="text-muted">
      [#assign values=fieldMaps[profile.id?string][field.name]/]
      [#if values?is_string]
        ${values}
      [#else]
      [#list values as v][#list field.properties?split(",") as pName]${v[pName]!} [/#list][#if v_has_next],[/#if][/#list]
      [/#if]
    </span>
    [/#list]
  [/#list]
  </div>
</div>
