[#ftl]
[@b.head/]
[@b.toolbar title="数据限制域元信息配置"]bar.addBack("${b.text("action.back")}");[/@]
[@b.form action=b.rest.save(dimension) theme="list"]
  [@b.textfield label="common.name" name="dimension.name" value="${dimension.name!}" required="true" maxlength="50"/]
  [@b.textfield name="dimension.title" value="${dimension.title!}" required="true" maxlength="50"/]
  [@b.radios name="dimension.valueType"  value=dimension.valueType/]
  [@b.textfield name="dimension.keyName" value="${dimension.keyName!}" maxlength="50" style="width:50px"/]
  [@b.textfield name="dimension.properties" value="${(dimension.properties)!}" style="width:300x;" maxlength="100" comment="多个属性用,分割"/]
  [@b.textarea name="dimension.source" rows="10" cols="80"  required="true" value="${(dimension.source)!}" style="width:400px;" maxlength="6000" comment="基本类型，此处可以text;组合类型使用json:/csv:"/]
  [@b.radios name="dimension.multiple"  value=dimension.multiple/]
  [@b.formfoot]
    [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit" /]
  [/@]
[/@]
[@b.foot/]