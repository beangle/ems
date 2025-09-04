[#ftl]
[@b.head/]
[@b.toolbar title="部门基本信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="table table-sm table-detail">
  <tr>
    <td class="title" width="20%">代码</td>
    <td >${depart.code}</td>
  </tr>
  <tr>
    <td class="title" width="20%">名称</td>
    <td >${depart.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">简称</td>
    <td >${depart.shortName!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">序号</td>
    <td >${depart.indexno}</td>
  </tr>
  <tr>
    <td class="title" width="20%">生效时间</td>
    <td  >${depart.beginOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">失效时间</td>
    <td  >${depart.endOn!}</td>
  </tr>
</table>

[@b.foot/]
