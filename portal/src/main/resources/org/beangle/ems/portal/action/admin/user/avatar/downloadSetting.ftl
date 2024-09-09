[@b.head/]
[#include "nav.ftl"/]
<div class="container-fluid">
  [@b.form name="avatarForm" action="!download" theme="list" target="_blank"]
    [@b.textarea name="code" value="" rows="20" cols="80" label="账户列表" placeholder="账户可以用回车，空格，逗号分割" maxlength="9999999"/]
    [@b.formfoot]
      [@b.submit value="下载"/]
    [/@]
  [/@]
</div>
[@b.foot/]
