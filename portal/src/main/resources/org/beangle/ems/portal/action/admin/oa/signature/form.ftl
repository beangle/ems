[@b.form action=b.rest.save(signature) theme="list"]
  [#if signature.user?? && signature.user.id??]
    [@b.field label="用户"]${signature.user.code!} ${signature.user.name!}[/@]
  [#else]
    [@ems.user label="用户" name="signature.user.id" required="true"/]
  [/#if]
  [@b.file label="签名图片" name="signature_file" extensions="png,jpg" required="true"/]
  [@b.formfoot]
    [@b.submit value="上传更新"/]
  [/@]
[/@]
[#list 1..10 as i]<br>[/#list]
