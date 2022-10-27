[#ftl/]
<table style="font-size:10pt" width="100%">
     <tbody><tr>
       <td width="75%">标题</td>
       <td width="25%">发布时间</td>
     </tr>
[#list notices as notice]
     <tr>
      <td>
        <a style="color:blue" alt="查看详情" target="_blank" href="${b.url("!info?id="+notice.id)}">
        ${notice.title}</a>
      </td>
      <td>${(notice.publishedAt?string("yyyy-MM-dd"))!}</td>
     </tr>
[/#list]
</tbody></table>
[@b.a href="!index" target="_blank"]&nbsp;更多...[/@]
