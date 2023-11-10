[#ftl]
[@b.head/]
[@b.toolbar title="新建/修改"]bar.addBack();[/@]
[@b.form action=b.rest.save(bundle) theme="list"]
  [@b.textfield name="bundle.name" label="名称" value=bundle.name! required="true" maxlength="200" style="width:300px"/]
  [@b.select name="bundle.app.id" label="应用" value=bundle.app! option="id,title" required="true" items=apps/]
  [@b.select label="语种" name="bundle.locale" items=locales value=(bundle.locale?string)!/]
  [@b.textarea name="bundle.texts" label="词条" value=bundle.texts maxlength="30000" rows="30" cols="80"/]
  [@b.formfoot]
   [@b.reset/]&nbsp;&nbsp;[@b.submit/]
  [/@]
[/@]
[@b.foot/]
