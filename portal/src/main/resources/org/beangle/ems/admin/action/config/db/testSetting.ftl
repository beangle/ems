[#ftl]
[@b.head/]
[@b.toolbar title="数据源连接测试"]
  bar.addBack("${b.text("action.back")}");
[/@]
[@b.form action="!test" theme="list" target="test_result" name="datasource_test_form"]
  [@b.field label="数据源"]${datasource.name}[/@]
  [@b.radios label="测试方法" items={"1":'使用密钥',"0":'使用密码'} name="use_credential" value="1" onclick="changeMethod(this.value)"/]
  [@b.select items=credentials name="credential.id" label="密码凭证" required="false" empty="..."/]
  [@b.textfield name="key" id="ds_key" label="AES密钥" required="false" /]
  [@b.textfield name="username" id="ds_username" label="用户名" required="false"/]
  [@b.textfield name="password" id="ds_password" label="密码" required="false"/]
  [@b.formfoot]
    <input type="hidden" value="${datasource.id}" name="db.id"/>
    [@b.submit value="测试" onsubmit="validKey"/]
  [/@]
[/@]
<script>
  function changeMethod(v){
    var form =document.datasource_test_form;
    if(v=='1'){
       $('#ds_username').parent().hide();
       $('#ds_password').parent().hide();
       $('#ds_key').parent().show();
    }else{
       $('#ds_username').parent().show();
       $('#ds_password').parent().show();
       $('#ds_key').parent().hide();
    }
  }
  changeMethod('1');
  function validKey(form){
     if(form['use_credential'].value=="1"){
        if(form['key'].value.length<1){
          alert("请输入key");return false;
        }
        if(form['credential.id'].value.length<1){
          alert("请选择凭证");return false;
        }
     }else{
        if(form['username'].value.length<1){
           alert("请输入用户名");return false;
        }
        if(form['password'].value.length<1){
           alert("请输入密码");return false;
        }
     }
    return true;
  }

</script>
[@b.div id="test_result"/]
[@b.foot/]
