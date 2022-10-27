[#ftl/]
<table style="font-size:10pt" width="100%">
     <tbody><tr>
       <td width="75%">文档标题</td>
       <td width="25%">发布时间</td>
     </tr>
     [#assign extMap={"xls":'application-vnd.ms-excel.png',"xlsx":'application-vnd.ms-excel.png',
      "doc":"application-msword.png","docx":"application-msword.png",
      "pdf":"application-pdf.png","zip":"application-zip.png",
      "rar":"application-zip.png","ppt":"application-vnd.ms-powerpoint.png"}]
[#list docs as doc]
     <tr>
      <td>
        <a style="color:blue" alt="查看详情" target="_blank" href="${b.url("!info?id="+doc.id)}">
        [#assign ext=doc.name?keep_after_last(".")/]
        <image style="width:16px" src="${b.static_url("bui","icons/48x48/mimetypes/"+extMap[ext]!"text-plain.png")}">
        ${doc.name}
        </a>
      </td>
      <td>${(doc.updatedAt?string("yyyy-MM-dd"))!}</td>
     </tr>
[/#list]
</tbody></table>
[@b.a href="!index" target="_blank"]&nbsp;更多...[/@]
