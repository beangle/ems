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
        <table><tr><td><img style="width:100%;height:230px" src="${b.static_url('local','images/bg.jpg')}"/></td></tr></table>
    </div>
    <div class="login">
   <img style="width:182px;height:35px;margin-top:22px;margin-bottom:23px" src="${b.static_url('local','images/system.jpg')}"/>
     <form name="loginForm" action="${base}/login" target="_top" method="post">
     [#if Parameters['sid_name']??]<input type="hidden" name="sid_name" value="${Parameters['sid_name']?html}">[/#if]
     [#if Parameters['service']??]<input type="hidden" name="service" value="${Parameters['service']?html}">[/#if]
     [#if Parameters['keyboard']??]<input type="hidden" name="keyboard" value="${Parameters['keyboard']?html}">[/#if]
        <table class="logintable">
            <tr style="height:30px">
                <td colspan="2" style="text-align:center;color:red;">${error!}</td>
            </tr>
            <tr>
                <td><label for="username">用户名:&nbsp;</label></td>
                <td>
                    <input name="username" id="username" tabindex="1" autofocus="autofocus" title="请输入用户名"  maxlength="18" placeholder="用户名" type="text" value="${(Parameters['username']?html)!}" style="width:105px;"/>
                </td>
            </tr>
            <tr>
                <td><label for="password_text">密　码:&nbsp;</label></td>
                <td>
                  <input id="password_text" name="password_text"  tabindex="2" type="password" style="width:105px;" autocomplete="off" placeholder="密码"/>
                  <input name="password" type="hidden"/>
                </td>
            </tr>
            [#if setting.enableCaptcha]
            <tr>
                <td><label for="captcha_response">验证码:&nbsp;</label></td>
                <td>
                  <input id="captcha_response" name="captcha_response" tabindex="3" type="text" style="width:50px;" placeholder="验证码"/>
                  <img src="${b.url("!captcha")}?t=${current_timestamp}" id="captcha_image" style="vertical-align:top;margin-top:1px;border:0px" width="90" height="25"  title="点击更换" onclick="change_captcha()">
                </td>
            </tr>
            [/#if]
            <tr>
                <td colspan="2">
                    <input type="submit" name="submitBtn" tabindex="6" class="blue-button"  onclick="return checkLogin(this.form)" value="登录"/>
                </td>
            </tr>
        </table>
        <table class="foottable">
            <tr>
                <td><img src="${b.static_url('local','images/weixin.png')}" height="80px"></td>
            </tr>
        </table>
     </form>
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
    var keyboardOption={usePreview :false,autoAccept:true,maxLength:20,display: { 'accept' : 'OK' }}
    $('#username').keyboard(keyboardOption);
    $('#password_text').keyboard(keyboardOption);
     [#if setting.enableCaptcha]
    $('#captcha_response').keyboard(keyboardOption);
      [/#if]
    [/#if]

    var form  = document.loginForm;
    function checkLogin(form){
        if(!form['username'].value){
            alert("用户名称不能为空");return false;
        }
        if(!(/^\w+$/.test(form['username'].value))){
            alert("用户名中只能包含数字,字母");return false;
        }
        if(!form['password_text'].value){
            alert("密码不能为空");return false;
        }
        [#if setting.enableCaptcha]
        if(!form['captcha_response'].value){
            alert("验证码不能为空");return false;
        }
        [/#if]
        try{
          var encryptedData = CryptoJS.AES.encrypt(form['password_text'].value, key, {mode: CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
          form['password_text'].disabled=true;
          form['password'].value=("?"+encryptedData.ciphertext);
        }catch(e){alert(e);return false;}
        return true;
    }

    function change_captcha(){
       document.getElementById('captcha_image').src="${b.url("!captcha")}?t="+(new Date()).getTime();
    }
</script>
</body>
</html>
