[#ftl/]
[@b.grid  items=permissions var="permission" sortable="false"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.edit")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove());
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col width="30%" property="description" title="描述" /]
    [@b.col width="10%" property="resource.title" title="对象" /]
    [@b.col width="10%" property="domain.title" title="领域" /]
    [@b.col width="40%"  title="其他约束" ]
    ${(permission.role.name)!}&nbsp;[#if permission.app??]${permission.app.title}&nbsp;[/#if]
    [#if permission.funcResource??]${permission.funcResource.title}&nbsp;[/#if]
    [/@]
  [/@]
[/@]
