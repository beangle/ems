[#ftl]
[@b.head/]
[@b.toolbar title="数据源信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">名称</td>
    <td class="content">${db.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">URL</td>
    <td class="content">${db.url!}</td>
  </tr>
  <tr>
    <td class="title">数据库驱动</td>
    <td class="content" >${db.driver!}</td>
  </tr>
  <tr>
    <td class="title">服务器</td>
    <td class="content" >${db.serverName!}</td>
  </tr>
  <tr>
    <td class="title">数据库名</td>
    <td class="content" >${db.databaseName!}</td>
  </tr>
  <tr>
    <td class="title">其他属性</td>
    <td class="content" >
    [#list db.properties?keys  as k]
      ${k}=${db.properties[k]}[#if k_has_next]<br/>[/#if]
    [/#list]
    </td>
  </tr>
  <tr>
    <td class="title">备注</td>
    <td class="content" >${db.remark!}</td>
  </tr>
</table>

[@b.foot/]
