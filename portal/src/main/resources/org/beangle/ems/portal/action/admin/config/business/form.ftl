[#ftl]
[@b.head/]
[@b.toolbar title="业务类型信息"]bar.addBack();[/@]
  [@b.form action=b.rest.save(business) theme="list"]
    [@b.textfield name="business.code" label="代码" value="${business.code!}" required="true" maxlength="50"/]
    [@b.textfield name="business.name" label="名称" value="${business.name!}" required="true" maxlength="80"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[@b.foot/]