[#ftl]
[@b.head/]
[@b.toolbar title="超级用户"]bar.addBack("${b.text("action.back")}");[/@]
[@b.form action=b.rest.save(root) theme="list"]
  [@b.textfield label="用户" name="user" value=(root.user.code)! required="true" maxlength="50"/]
  [@b.startend label="有效期"
    name="root.beginOn,root.endOn" required="true,false"
    start=root.beginOn end=root.endOn format="date"/]
  [@b.formfoot]
    [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit" /]
  [/@]
[/@]
[@b.foot/]
