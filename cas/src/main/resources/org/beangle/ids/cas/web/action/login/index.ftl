<!DOCTYPE html>
<html lang="zh_CN">
  <head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta http-equiv="pragma" content="no-cache"/>
    <meta http-equiv="cache-control" content="no-cache"/>
    <meta http-equiv="content-style-type" content="text/css"/>
    <meta http-equiv="content-script-type" content="text/javascript"/>
    <meta http-equiv="expires" content="0"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Login</title>
    ${b.css("bootstrap","css/bootstrap.min.css")}
    ${b.css("font-awesome","css/all.min.css")}
    ${b.css("ems","css/login.css")}
    [#if Parameters['keyboard']??]
    ${b.css("virtual-keyboard","dist/css/keyboard.min.css")}
    ${b.css("virtual-keyboard","dist/css/keyboard-basic.min.css")}
    [/#if]
  </head>
<body>

<div class="logindiv">
    <div class="bulletin">
        <img style="width:100%;height:80px" src="${b.static_url('local','images/banner.jpg')}"/>
        <table style="width:100%;"><tr><td><img style="width:100%;height:230px" src="${b.static_url('local','images/bg.jpg')}"/></td></tr></table>
    </div>
    <div class="login">
     <img style="width:200px;height:35px;margin-top:20px;margin-bottom:23px" src="${b.static_url('local','images/system.png')}"/>
     <form name="loginForm" action="${b.base}/login" target="_top" method="post">
     [#if Parameters['sid_name']??]<input type="hidden" name="sid_name" value="${Parameters['sid_name']?html}">[/#if]
     [#if Parameters['service']??]<input type="hidden" name="service" value="${Parameters['service']?html}">[/#if]
     [#if Parameters['keyboard']??]<input type="hidden" name="keyboard" value="${Parameters['keyboard']?html}">[/#if]
        <div style="text-align:center;color:red;margin-top: -24px;max-width:210px;" id="error_msg">${error!'&nbsp;'}</div>
        <div style="border: 0px;border-bottom:1px #7DC4DB solid;margin:auto;width:220px">
            [#if setting.remoteLogoutUrl?? && (setting.displayLoginSwitch!false)]
            <div>
                  <input type="radio" name="loginType" value="local" checked="checked" id="local_login"><label for="local_login">本地登录</label>
                  <input type="radio" name="loginType"  onchange="remoteLogin(this,this.form)" value="remote" id="remote_login"><label for="remote_login">统一身份认证</label>
            </div>
            [/#if]
                  <div class="col-auto">
                    <div class="input-group mb-1">
                      <div class="input-group-prepend"><div class="input-group-text" style=""><i class="fas fa-user" style="width: 16px;"></i></div></div>
                      <input name="username" id="username" tabindex="1" autofocus="autofocus" class="form-control" placeholder="${b.text('ui.login.username.tip')}" type="text" value="">
                    </div>
                  </div>
                  <div class="col-auto">
                    <div class="input-group mb-1">
                      <div class="input-group-prepend"><div class="input-group-text" ><i class="fas fa-key" style="width: 16px;"></i></div></div>
                      <input name="password_text" id="password_text" tabindex="2" autocomplete="off" class="form-control" placeholder="[#if setting.passwordReadOnly && setting.remoteLogoutUrl??]统一身份认证密码[#else]密码[/#if]" type="password" value="">
                      <input name="password" type="hidden"/>
                    </div>
                  </div>
            [#if setting.enableCaptcha]
                  <div class="col-auto">
                    <div class="input-group mb-1">
                      <div class="input-group-prepend"><div class="input-group-text"><i class="fas fa-font" style="width: 16px;"></i></div></div>
                      <input name="captcha_response" id="captcha_response" tabindex="3" class="form-control" type="text" value="" placeholder="图片验证码">
                      <div class="input-group-append"><div class="input-group-text" style="padding: 0px;background-color: white;">
                        <img src="${captcha_url}?t=${current_timestamp}" id="captcha_image" title="点击更换" onclick="change_captcha()" style="vertical-align:top;margin:0px;border:0px" height="23px">
                      </div></div>
                    </div>
                  </div>
            [/#if]
            <div class="col-auto">
              <div class="input-group mb-1">
                    [#if setting.enableSmsLogin]
                    <div style="padding-top: 5px;margin-right: 40px;"><a href="javascript:void(0)" onclick="changeLogin()" style="font-size:0.8em;color:#515151;">短信登录</a></div>
                    [#else]
                    <div style="padding-top: 5px;margin-right: 40px;"><a href="safety.html" target="_blank" style="font-size:0.8em;color:#515151;">隐私安全</a></div>
                    [/#if]
                    <input type="submit" name="submitBtn" tabindex="6" class="btn btn-primary btn-sm"  onclick="return checkLogin(this.form)" value="登录"/>
              </div>
            </div>
         </div>
        </form>
        <table class="foottable">
            <tr>
                <td><img src="${b.static_url('local','images/weixin.jpg')}" height="75px"></td>
            </tr>
        </table>
   </div>
</div>
${b.script("cryptojs","rollups/aes.js")}
${b.script("cryptojs","components/mode-ecb.js")}
[#if Parameters['keyboard']??]
${b.script("jquery","jquery.min.js")}
${b.script("virtual-keyboard","dist/js/jquery.keyboard.min.js")}
[/#if]
<script type="text/javascript">
    var key= location.hostname;
    if(key.length>=16) key= key.substring(0,16);
    else  key= (key+'0'.repeat(16-key.length));
    key=CryptoJS.enc.Utf8.parse(key);

   [#if Parameters['keyboard']??]
    var keyboardOption={usePreview :false,autoAccept:true,maxLength:50,display: { 'accept' : 'OK' }}
    $('#username').keyboard(keyboardOption);
    $('#password_text').keyboard(keyboardOption);
     [#if setting.enableCaptcha]
    $('#captcha_response').keyboard(keyboardOption);
      [/#if]
    [/#if]

    var form  = document.loginForm;
    function checkLogin(form){
        if(!form['username'].value){
            displayError("用户名称不能为空");return false;
        }
        if(!(/^\w+$/.test(form['username'].value))){
            displayError("用户名中只能包含数字,字母");return false;
        }
        if(!form['password_text'].value){
            displayError("密码不能为空");return false;
        }
        [#if setting.enableCaptcha]
        if(!form['captcha_response'].value){
            displayError("图片验证码不能为空");return false;
        }
        [/#if]

        try{
          var encryptedData = CryptoJS.AES.encrypt(form['password_text'].value, key, {mode: CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
          form['password_text'].disabled=true;
          form['password'].value=("?"+encryptedData.ciphertext);
        }catch(e){alert(e);return false;}
        addHidden(form,"local","1");
        return true;
    }

[#if setting.enableCaptcha]
    function change_captcha(){
       document.getElementById('captcha_image').src="${captcha_url}?t="+(new Date()).getTime();
    }
[/#if]
    function remoteLogin(elem,form){
      if(elem.checked){
        addHidden(form,"remote","1");
        form.submit();
      }
    }
    function addHidden(form,name,value){
      if(!form[name]){
        var input = document.createElement('input');
        input.setAttribute("name",name);
        input.setAttribute("value",value);
        input.setAttribute("type","hidden");
        form.appendChild(input);
      }else{
        form[name].value=value;
      }
    }
    function displayError(msg){
        document.getElementById("error_msg").innerHTML=msg;
    }
    function changeLogin(){
      form.action="${b.base}/sms-login";
      addHidden(form,"local","1");
      form.submit();
    }
</script>
</body>
</html>
