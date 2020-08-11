[#ftl]
[@b.head/]
[@b.grid items=credentials var="credential"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="name" title="名称"/]
    [@b.col width="10%" property="username" title="用户名"]${credential.username!}[/@]
    [@b.col width="35%" property="password" title="密文"]${credential.password!}[/@]
    [@b.col width="15%" property="expiredAt" title="过期日期"]${credential.expiredAt?string('yy-MM-dd HH:mm')}[/@]
    [@b.col width="15%" property="updatedAt" title="更新时间"]${credential.updatedAt?string('yy-MM-dd HH:mm')}[/@]
  [/@]
[/@]
[@b.foot/]
