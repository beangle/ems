[#ftl]
[@b.head/]
  [#if passwordConfigs?size==0]
    没有密码配置 [@b.a href="!editNew"]创建一个[/@]
  [#else]
    [@b.toolbar title="密码配置"]
        bar.addItem("修改",function(){
         bg.form.submit(document.pwdConfigForm);
        });
    [/@]
    [#assign config = passwordConfigs?first]
    <div class="card">
      <div class="card-body">
        <h5>密码长度</h5>
        <ul class="list-group">
          <li class="list-group-item">密码最小长度: ${config.minlen}</li>
          <li class="list-group-item">密码最大长度: ${config.maxlen}</li>
        </ul>
        <h5>密码保留期限</h5>
        <ul class="list-group">
          <li class="list-group-item">可更改的最少天数: ${config.mindays} (0表示密码随时可改)</li>
          <li class="list-group-item">保持有效的最长天数: ${config.maxdays}</li>
          <li class="list-group-item">到期前，提前收到警告信息的天数: ${config.warnage}</li>
          <li class="list-group-item">停滞的天数: ${config.idledays} (密码到期后仍可登录，但需要立即更改密码。如未更改，超过保留天数则不能登录)</li>
        </ul>
        <h5>密码质量</h5>
        <ul class="list-group">
          <li class="list-group-item">至少含有${config.dcredit}数字,${config.lcredit}小写字母，${config.ucredit}大写字母，${config.ocredit}其他字符</li>
          <li class="list-group-item">至少包含上述${config.minclass}类字符</li>
          <li class="list-group-item">是否检查密码中包含用户名: ${config.usercheck?string('是','否')}</li>
        </ul>
      </div>
    </div>
    [@b.form name="pwdConfigForm" action="!edit?id="+config.id/]
  [/#if]
[@b.foot/]
