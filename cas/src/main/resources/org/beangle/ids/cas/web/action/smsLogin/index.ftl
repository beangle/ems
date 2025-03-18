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
    <base href="${b.base}/"/>
    ${b.static.load(["jquery"])}
  </head>
<body>

<div class="logindiv">
    <div class="bulletin">
        <img style="width:100%;height:80px" src="${b.static_url('local','images/banner.jpg')}"/>
        <table style="width:100%;"><tr><td><img style="width:100%;height:230px" src="${b.static_url('local','images/bg.jpg')}"/></td></tr></table>
    </div>
    <div class="login">
     <img style="width:200px;height:35px;margin-top:20px;margin-bottom:23px" src="${b.static_url('local','images/system.jpg')}"/>
     <form name="loginForm" action="${b.base}/sms-login" target="_top" method="post">
     [#if Parameters['sid_name']??]<input type="hidden" name="sid_name" value="${Parameters['sid_name']?html}">[/#if]
     [#if Parameters['service']??]<input type="hidden" name="service" value="${Parameters['service']?html}">[/#if]
        <div style="text-align:center;color:red;margin-top: -24px;max-width:210px;" id="error_msg">${error!'&nbsp;'}</div>
        <div style="border: 0px;border-bottom:1px #7DC4DB solid;margin:auto;width:220px">
                  <div class="col-auto">
                    <div class="input-group mb-1">
                      <div class="input-group-prepend"><div class="input-group-text" style=""><i class="fas fa-user" style="width: 16px;"></i></div></div>
                      <input name="username" id="username" tabindex="1" autofocus="autofocus" class="form-control" placeholder="${b.text('ui.login.username.tip')}" type="text" value="${Parameters['username']!}">
                    </div>
                  </div>
                  <div class="col-auto">
                    <div class="input-group mb-1">
                      <div class="input-group-prepend"><div class="input-group-text" ><i class="fas fa-key" style="width: 16px;"></i></div></div>
                      <input name="smsCode" tabindex="2" class="form-control" placeholder="短信验证码" value="" maxlength="6">
                      <input type="button"  tabindex="3" class="btn btn-outline-primary btn-sm"  onclick="send(this.form);return false;" value="发送" style="margin-left: 2px;"/>
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
                    <div style="padding-top: 5px;margin-right: 50px;"><a href="javascript:void(0)" onclick="changeLogin()" style="font-size:0.8em;color:#515151;">密码登录</a></div>
                    <input type="submit" name="submitBtn" tabindex="4" class="btn btn-primary btn-sm"  onclick="return checkLogin(this.form)" value="登录"/>
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
<script type="text/javascript">
    var form  = document.loginForm;
    function send(form){
      if(!form['username'].value){
          displayError("用户名称不能为空");return false;
      }
      if(!(/^\w+$/.test(form['username'].value))){
          displayError("用户名中只能包含数字,字母");return false;
      }
      $.get("${b.url('!send')}?username="+form['username'].value,function(data,status){
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
    }
    function changeLogin(){
      form.action="${b.base}/login";
      addHidden(form,"local","1");
      form.submit();
    }
</script>
</body>
</html>
