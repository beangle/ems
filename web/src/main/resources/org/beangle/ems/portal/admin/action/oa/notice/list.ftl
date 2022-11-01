[#ftl]
[@b.head/]

[@b.grid items=notices var="notice"]
  [@b.gridbar]
    bar.addItem("${b.text("action.info")}",action.info());
    bar.addItem("起草",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col property="title" title="标题"]
      [@b.a href="!info?id=${notice.id}"]
      [#if notice.docs?size>0]<image src="${b.static_url('bui',"icons/16x16/actions/paperclip.png")}"/>[/#if]
      <span [#if notice.archived] style="color:#999999" title="已归档"[/#if]>${notice.title!}</span>
      [/@]
      [#if notice.popup]<sup>弹窗</sup>[/#if]
      [#if notice.sticky]<sup>置顶</sup>[/#if]
    [/@]
    [@b.col width="10%" property="issuer" title="部门"/]
    [@b.col width="12%" property="app.title" title="应用"/]
    [@b.col width="14%" title="用户类别"]
      <span style="font-size:0.8em">[#list notice.categories?sort_by('code') as uc]${uc.name}[#if uc_has_next]&nbsp;[/#if][/#list]</span>
    [/@]
    [@b.col width="10%" property="createdAt" title="起草日期"]${notice.operator.name} ${notice.createdAt?string("yy-MM-dd")}[/@]
    [@b.col width="10%" title="有效期"]${notice.beginOn?string("MM-dd")}~${notice.endOn?string("MM-dd")}[/@]
    [@b.col width="8%" property="status" title="状态"]${notice.status.title}[/@]
  [/@]
[/@]
[@b.foot/]
