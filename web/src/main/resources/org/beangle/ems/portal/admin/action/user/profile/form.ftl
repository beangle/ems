[#ftl]
[@b.head/]
[@b.toolbar title="数据授权"]
  function save(){if(confirm("确定设置?")){bg.form.submit(document.profileForm);}}
  bar.addItem("${b.text("action.save")}",save);
  bar.addBack();
[/@]
[@b.form name="profileForm" action="!save" theme="list"]
  [@b.textfield label="名称" value=profile.name!'默认' name="profile.name" required="true"/]
  [@b.field label="数据项目"]
  [#if fields?size==0]该系统没有增加涉及到的数据维度[/#if]
  <div class="container">
  [@b.tabs]
    [#list fields?sort_by("title") as field]
    [@b.tab label="${field.title}(${field.name})"]
    [#if ignoreDimensions?seq_contains(field) && field.multiple]
    <div>
      <input name="ignoreDimension${field.id}" type="radio" value="1" [#if userIgnoreDimensions?seq_contains(field)]checked="checked"[/#if] id="ignoreDimension${field.id}_1"><label for="ignoreDimension${field.id}_1">使用通配符*</label>
      <input name="ignoreDimension${field.id}" type="radio" value="0" [#if !userIgnoreDimensions?seq_contains(field)]checked="checked"[/#if] id="ignoreDimension${field.id}_2"><label for="ignoreDimension${field.id}_2">选择或填写具体值</label>
    </div>
    [/#if]
    [#if  field.valueType]
      <table class="grid" width="100%">
        <tr><td colspan="2"><input type="text" name="${field.name}" value="${userDimensions[field.name]!}"/>[#if field.multiple]多个值请用,格开[/#if]</td></tr>
      </table>
    [#else]
      [#assign boxtype="radio"]
      [#if field.multiple ]
      [#assign boxtype="checkbox"]
      [/#if]
      [@b.grid items=mngDimensions[field.name] var="value"]
        [@b.row]
          [#assign checked=false/]
          [#list userDimensions[field.name]?if_exists as userValue]
            [#if (userValue[field.keyName]!"")?string == value[field.keyName]?string]
            [#assign checked=true/]
            [#break/]
           [/#if]
          [/#list]
          [@b.boxcol property=field.keyName boxname=field.name checked=checked type=boxtype/]
          [#if field.properties??]
          [#list field.properties?split(",") as pName][@b.col title=pName]${value[pName]!}[/@][/#list]
          [#else]
          [@b.col title="可选值"]${value}[/@]
          [/#if]
        [/@]
      [/@]
    [/#if]
    [/@]
    [/#list]
  [/@]
  </div>
  [/@]
  [@b.formfoot]
  [#if profile.persisted]
    <input type="hidden" name="profile.id" value="${profile.id!}"/>
    [#else]
    <input type="hidden" name="profile.user.id" value="${profile.user.id}"/>
    [/#if]
      <input type="hidden" name="_params" value="&profile.user.id=${profile.user.id}"/>
  [/@]
[/@]
[@b.foot/]
