[@b.head/]
  [@b.toolbar title="修改账户密码"]
    [#if Parameters['updated']??]
    bar.addItem("退出","logout()")
    function logout(){
      document.location="${b.url('logout')}"
    }
   [/#if]
    bar.addClose();
  [/@]
  <div class="container">
   [@b.form name="accountForm" action="!save" theme="list" title="修改账户密码" ]
     [@b.field label="账户"]${principal.name} ${principal.description}[/@]
     [#if principal.remoteToken??]
     [@b.field label="密码修改建议"]建议新密码和统一身份认证密码一致。[/@]
     [/#if]
     [@b.password label="新密码" id="password"  name="password" maxlength="15" onblur="checkPassword(this.value)" required="true" comment="请输入包含数字、大写字母、小写字母、特殊符号的至少两种的、8位以上的密码"/]
     [@b.password label="重复新密码" name="password2" maxlength="15" required="true"/]
     [@b.formfoot]
       [#if Parameters['service']??]
       <input type="hidden" name="service" value="${Parameters['service']?html}"/>
       [/#if]
       [@b.submit value="提交" onsubmit="valid" id="submit_button" disabled="disabled"/]
     [/@]
   [/@]
   <script>
     var comment="";
     var stengthOK=false;
     jQuery(document).ready(function(){
       $.get("${emsapi}/platform/user/credentials/comment.json", function(result){
         comment=result;
         jQuery("#password").next("label").html(comment)
       });
     });
     function valid(form){
       if(form['password2'].value!=form['password'].value){
         alert("新密码和重复密码不一致");
         return false;
       }
       return true;
     }

     function displayMessage(){
       if(stengthOK){
         jQuery("#password").next("label").html("<span style='color:green'>密码符合强度要求</span>");
         jQuery("#submit_button").enable()
       }else{
         jQuery("#password").next("label").html("<span style='color:red'>密码不符合强度要求</span>,请输入"+comment);
         jQuery("#submit_button").enable(false)
       }
     }

     function checkPassword(password){
       $.post('${emsapi}/platform/user/credentials/check.json',{user:"${principal.name}",pwd:password}, function(data) {stengthOK=data;displayMessage();},'json');
     }
   </script>
  </div>
[@b.foot/]
