[#ftl]
[@b.head/]
[@b.toolbar title="角色数据配置"]
  function save(){if(confirm("确定设置?")){bg.form.submit(document.profileForm);}}
  bar.addItem("${b.text("action.save")}",save);
    [#--function cancelEdit(){bg.form.submit(document.profileForm,'${b.url("!profile")}')}
  bar.addItem("取消",cancelEdit);--]
[/@]
[@b.form name="profileForm" action="!saveProfile"]
  <input type="hidden" name="role.id" value="${role.id}"/>
  [@b.tabs]
    [#list fields?sort_by("title") as field]
    [@b.tab label="${field.name}(${field.title})"]
    [#if ignoreDimensions?seq_contains(field)]
    <div>
      <input name="ignoreDimension${field.id}" type="radio" value="1" [#if holderIgnoreDimensions?seq_contains(field)]checked="checked"[/#if] id="ignoreDimension${field.id}_1"><label for="ignoreDimension${field.id}_1">使用通配符*</label>
      <input name="ignoreDimension${field.id}" type="radio" value="0" [#if !holderIgnoreDimensions?seq_contains(field)]checked="checked"[/#if] id="ignoreDimension${field.id}_2"><label for="ignoreDimension${field.id}_2">选择或填写具体值</label>
    </div>
    [/#if]
    [#if field.multiple && field.keyName?exists]
      [@b.grid items=mngDimensions[field.name] var="value"]
        [@b.row]
          [#assign checked=false/]
          [#list aoDimensions[field.name]?if_exists as userValue]
            [#if (userValue[field.keyName]!"")?string == value[field.keyName]?string]
            [#assign checked=true/]
            [#break/]
           [/#if]
          [/#list]
          [@b.boxcol property=field.keyName boxname=field.name checked=checked/]
          [#if field.properties??]
          [#list field.properties?split(",") as pName][@b.col title=pName]${value[pName]!}[/@][/#list]
          [#else]
          [@b.col title="可选值"]${value}[/@]
          [/#if]
        [/@]
      [/@]
    [#else]
    <table class="grid" width="100%">
      <tr><td colspan="2"><input type="text" name="${field.name}" value="${aoDimensions[field.name]!}"/>[#if field.multiple]多个值请用,格开[/#if]</td></tr>
    </table>
    [/#if]
    [/@]
    [/#list]
  [/@]
[/@]
[@b.foot/]
