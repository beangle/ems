[#ftl]
[@b.head/]
[@b.toolbar title="新增用户"]bar.addBack("${b.text("action.back")}");[/@]
[@b.messages/]
[@b.form name="userForm" action="!save" class="listform" theme="list"]
    [@b.textfield name="user.code"label="账户"  style="width:200px;" required="true" maxlength="30"/]
    [@b.textfield name="user.name" label="姓名" value="" style="width:200px;" required="true" maxlength="50" /]
    [@b.select name="user.category.id" label="身份" items=categories required="true" /]
    [@b.radios name="user.enabled"  value=user.enabled items="1:action.activate,0:action.freeze"/]
    [@b.radios name="user.locked" value=user.locked items="1:锁定,0:解锁"/]
    [@b.password label="user.password" name="password" value="" maxlength="20"  required="true" showStrength="true"/]
    [@b.startend label="有效期" name="user.beginOn,user.endOn" required="true,false" start=user.beginOn end=user.endOn format="date"/]
    [@b.textarea cols="50" rows="1" label="备注" name="user.remark" maxlength="50"/]
    [@b.formfoot][@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/][/@]
[/@]
[@b.foot/]