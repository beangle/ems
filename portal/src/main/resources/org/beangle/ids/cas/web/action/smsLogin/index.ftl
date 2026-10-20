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
    ${b.css("ems-shell","css/login.css")}
    <base href="${b.base}/"/>
  </head>
<body class="login-page">

<div class="logindiv">
  <div class="bulletin">
    <img style="width:100%;height:80px" src="${b.static_url('local','images/banner.jpg')}"/>
    <table style="width:100%;"><tr><td><img style="width:100%;height:230px" src="${b.static_url('local','images/bg.jpg')}"/></td></tr></table>
  </div>
  <div class="login">
    <img style="width:200px;height:35px;margin-top:20px;margin-bottom:23px" src="${b.static_url('local','images/system.png')}"/>
    <form name="loginForm" action="${b.base}/cas/sms-login" target="_top" method="post">
      [#if Parameters['sid_name']??]<input type="hidden" name="sid_name" value="${Parameters['sid_name']?html}">[/#if]
      [#if Parameters['service']??]<input type="hidden" name="service" value="${Parameters['service']?html}">[/#if]
      <input type="hidden" name="local" value="1">
      <div style="text-align:center;color:red;margin-top: -24px;max-width:210px;" id="error_msg">${error!'&nbsp;'}</div>
      <div style="border: 0px;border-bottom:1px #7DC4DB solid;margin:auto;width:220px">
        <div class="col-auto">
          <div class="input-group mb-1">
            <div class="input-group-text">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-person" viewBox="0 0 16 16" aria-hidden="true" focusable="false">
                <path d="M8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6m2-3a2 2 0 1 1-4 0 2 2 0 0 1 4 0m4 8c0 1-1 1-1 1H3s-1 0-1-1 1-4 6-4 6 3 6 4m-1-.004c-.001-.246-.154-.986-.832-1.664C11.516 10.68 10.289 10 8 10s-3.516.68-4.168 1.332c-.678.678-.83 1.418-.832 1.664z"/>
              </svg>
            </div>
            <input name="username" id="username" tabindex="1" autofocus="autofocus" class="form-control" placeholder="${b.text('ui.login.username.tip')}" type="text" value="${Parameters['username']!}">
          </div>
        </div>
        <div class="col-auto">
          <div class="input-group mb-1">
            <div class="input-group-text">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-key" viewBox="0 0 16 16" aria-hidden="true" focusable="false">
                <path d="M0 8a4 4 0 0 1 7.465-2H14a.5.5 0 0 1 .354.146l1.5 1.5a.5.5 0 0 1 0 .708l-1.5 1.5a.5.5 0 0 1-.708 0L13 9.207l-.646.647a.5.5 0 0 1-.708 0L11 9.207l-.646.647a.5.5 0 0 1-.708 0L9 9.207l-.646.647A.5.5 0 0 1 8 10h-.535A4 4 0 0 1 0 8m4-3a3 3 0 1 0 2.712 4.285A.5.5 0 0 1 7.163 9h.63l.853-.854a.5.5 0 0 1 .708 0l.646.647.646-.647a.5.5 0 0 1 .708 0l.646.647.646-.647a.5.5 0 0 1 .708 0l.646.647.793-.793-1-1h-6.63a.5.5 0 0 1-.451-.285A3 3 0 0 0 4 5"/>
                <path d="M4 8a1 1 0 1 1-2 0 1 1 0 0 1 2 0"/>
              </svg>
            </div>
            <input name="smsCode" tabindex="2" class="form-control" placeholder="短信验证码" value="" maxlength="6">
            <input type="button"  tabindex="3" class="btn btn-outline-primary btn-sm"  onclick="send(this.form);return false;" value="发送" style="margin-left: 2px;"/>
          </div>
        </div>
        [#if setting.enableCaptcha]
        <div class="col-auto">
          <div class="input-group mb-1">
            <div class="input-group-text">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-fonts" viewBox="0 0 16 16" aria-hidden="true" focusable="false">
                <path d="M12.258 3h-8.51l-.083 2.46h.479c.26-1.544.758-1.783 2.693-1.845l.424-.013v7.827c0 .663-.144.82-1.3.923v.52h4.082v-.52c-1.162-.103-1.306-.26-1.306-.923V3.602l.431.013c1.934.062 2.434.301 2.693 1.846h.479z"/>
              </svg>
            </div>
            <input name="captcha_response" id="captcha_response" tabindex="3" class="form-control" type="text" value="" placeholder="图片验证码" onblur="validate_captcha(this.value)">
            <div class="input-group-text" style="padding: 0px;background-color: white;">
              <img src="${captcha_url}?t=${current_timestamp}" id="captcha_image" title="点击更换" onclick="change_captcha()" style="vertical-align:top;margin:0px;border:0px" height="23px">
            </div>
          </div>
        </div>
        [/#if]
        <div class="col-auto">
          <div class="input-group mb-1" style="justify-content:center;gap:20px;">
            <a href="javascript:void(0)" onclick="changeLogin()" style="font-size:0.8em;color:#515151;margin-top: auto;">密码登录</a>
            <input type="submit" name="submitBtn" tabindex="4" class="btn btn-primary btn-sm"  onclick="return checkLogin(this.form)" value="登录"/>
          </div>
        </div>
      </div>
    </form>
    <table class="foottable">
      <tr>
        <td><img src="${b.static_url('local','images/weixin.jpg')}" height="75px"></td>
        <td><a href="${b.base}/cas/safety.html" target="_blank" style="font-size:0.8em;color:#515151;">隐私安全</a></td>
      </tr>
    </table>
  </div>
</div>
<script type="text/javascript">
    var form  = document.loginForm;
    function send(form){
      if(!form['username'].value){
          displayError("用户名称不能为空");return false;
      }
      if(!(/^\w+$/.test(form['username'].value))){
          displayError("用户名中只能包含数字,字母");return false;
      }
      fetch("${b.url('!send')}?username="+encodeURIComponent(form['username'].value))
        .then(function(response){ return response.text(); })
        .then(function(data){
          displayError(data);
        });
    }
    function checkLogin(form){
      if(!form['username'].value){
          displayError("用户名称不能为空");return false;
      }
      if(!form['smsCode'].value){
          displayError("短信验证码不能为空");return false;
      }
      [#if setting.enableCaptcha]
      if(!form['captcha_response'].value){
          displayError("图片验证码不能为空");return false;
      }
      [/#if]
      return true;
    }
[#if setting.enableCaptcha]
    function change_captcha(){
      document.getElementById('captcha_image').src="${captcha_url}?t="+(new Date()).getTime();
    }
    function validate_captcha(response){
      if(response){
        fetch("${captcha_check_url}".replace("{response}",response))
          .then(function(response){ return response.json(); })
          .then(function(data){
            if(data.code==200){
              displayInfo("验证码正确");
            }else{
              displayError("验证码不正确");
            }
          });
      }
    }
[/#if]
    function addHidden(form,name,value){
      var input = document.createElement('input');
      input.setAttribute("name",name);
      input.setAttribute("value",value);
      input.setAttribute("type","hidden");
      form.appendChild(input);
    }
    function displayError(msg){
      document.getElementById("error_msg").innerHTML=msg;
      document.getElementById("error_msg").style.color="red";
    }
    function displayInfo(msg){
      document.getElementById("error_msg").innerHTML=msg;
      document.getElementById("error_msg").style.color="green";
    }
    function changeLogin(){
      form.action="${b.base}/cas/login";
      addHidden(form,"local","1");
      form.submit();
    }
</script>
</body>
</html>
