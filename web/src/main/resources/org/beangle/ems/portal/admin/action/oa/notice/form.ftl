[#ftl]
[@b.head/]
[#if notice.status.title="审核通过"]
[@b.toolbar title="修改通知公告"]bar.addBack();[/@]
<p class="bg-danger">该公告已经审核通过，不能更改，如需更改需审核环节退回</p>
  [#include "info_panel.ftl"/]
[#else]
[@b.toolbar title="新建/修改通知公告"]bar.addBack();[/@]
[@b.form action=b.rest.save(notice) theme="list"]
  [@b.textfield name="notice.title" label="标题" value=notice.title! required="true" maxlength="100"/]
  [@b.textfield name="notice.issuer" label="发布部门" value=notice.issuer! required="true" maxlength="40"/]
  [@b.select name="notice.app.id" label="应用" value=notice.app option="id,title" required="true" items=apps?sort_by('title')/]
  [@b.checkboxes name="category.id" label="面向用户" value=notice.categories required="true" items=categories/]
  [@b.radios name="notice.sticky" label="是否置顶" value=notice.sticky required="true" /]
  [@b.radios name="notice.popup" label="是否弹窗" value=notice.popup required="true" /]
  [@b.startend label="有效期限" name="notice.beginOn,notice.endOn" required="true,true" start=notice.beginOn end=notice.endOn format="date"/]
  [@b.editor name="notice.contents" id="notice_content" label="内容" rows="20" cols="80" value=notice.contents maxlength="30000" required="true"/]

  [#list notice.docs as d]
  [@b.field label="附件"]${d.name}[/@]
  [/#list]

  [#list 1..(3-notice.docs?size) as i]
  [@b.file label="附件"+i name="notice_doc" maxSize="20MB" extensions="doc,docx,pdf,xls,xlsx,zip,rar,png,jpg"/]
  [/#list]
  [@b.formfoot]
   [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
  [/@]
[/@]
[/#if]
[@b.foot/]
