[#ftl]
[@b.head/]
[@b.toolbar title="修改应用分组"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action=b.rest.save(appGroup)  theme="list"]
    [@b.textfield name="appGroup.indexno" label="分类号" value="${appGroup.indexno!}" required="true" maxlength="50"/]
    [@b.textfield name="appGroup.name" label="名称" value="${appGroup.name!}" required="true" maxlength="200"/]
    [@b.textfield name="appGroup.title" label="标题" value="${appGroup.title!}" required="true" maxlength="200"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]
