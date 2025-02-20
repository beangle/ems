[#ftl]
[@b.head/]
[@b.toolbar title="部门基本信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">代码</td>
    <td class="content">${depart.code}</td>
  </tr>
  <tr>
    <td class="title" width="20%">名称</td>
    <td class="content">${depart.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">简称</td>
    <td class="content">${depart.shortName!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">序号</td>
    <td class="content">${depart.indexno}</td>
  </tr>
  <tr>
    <td class="title" width="20%">生效时间</td>
    <td class="content" >${depart.beginOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">失效时间</td>
    <td class="content" >${depart.endOn!}</td>
  </tr>
</table>

[@b.foot/]
