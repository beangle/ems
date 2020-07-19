[#ftl]
[@b.head/]
<style>
  form.listform label.title{
    width:250px;
  }
</style>
[@b.toolbar title="密码配置信息"]bar.addBack();[/@]
  [@b.form action=b.rest.save(passwordConfig) title="密码配置" theme="list"]
    [@b.textfield name="passwordConfig.minlen" label="最小长度" value=passwordConfig.minlen required="true" maxlength="3"/]
    [@b.textfield name="passwordConfig.maxlen" label="最大长度" value=passwordConfig.maxlen required="true" maxlength="3"/]

    [@b.textfield name="passwordConfig.mindays" label="可更改的最少天数" value=passwordConfig.mindays required="true" maxlength="3" comment="0表示密码随时可改"/]
    [@b.textfield name="passwordConfig.maxdays" label="保持有效的最长天数" value=passwordConfig.maxdays required="true" maxlength="3"/]
    [@b.textfield name="passwordConfig.warnage" label="到期前，提前收到警告信息的天数" value=passwordConfig.warnage required="true" maxlength="3"/]
    [@b.textfield name="passwordConfig.idledays" label="停滞的天数" value=passwordConfig.idledays required="true" maxlength="3" comment="密码到期后仍可登录，但需要立即更改密码。如未更改，超过保留天数则不能登录"/]

    [@b.textfield name="passwordConfig.dcredit" label="最少含有多少个数字" value=passwordConfig.dcredit required="true" maxlength="3"/]
    [@b.textfield name="passwordConfig.lcredit" label="最少含有多少个小写字母" value=passwordConfig.lcredit required="true" maxlength="3"/]
    [@b.textfield name="passwordConfig.ucredit" label="最少含有多少个大写字母" value=passwordConfig.ucredit required="true" maxlength="3"/]
    [@b.textfield name="passwordConfig.ocredit" label="最少含有多少个其他字母" value=passwordConfig.ocredit required="true" maxlength="3"/]
    [@b.textfield name="passwordConfig.minclass" label="最少含有上述几类字符" value=passwordConfig.minclass required="true" maxlength="3"/]

    [@b.radios name="passwordConfig.usercheck" label="是否检查密码中含有用户名" value=passwordConfig.usercheck required="true" maxlength="3"/]

    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[@b.foot/]
