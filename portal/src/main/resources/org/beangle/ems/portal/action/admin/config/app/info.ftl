[#ftl]
[@b.head/]
[@b.toolbar title="应用信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="table table-sm table-detail">
  <tr>
    <td class="title" width="15%">名称</td>
    <td>${app.name}</td>
    <td class="title" width="15%">标题</td>
    <td>${app.indexno} ${app.title}</td>
  </tr>
  <tr>
    <td class="title">分组</td>
    <td>${app.group.title}(${app.group.name})</td>
    <td class="title">类型</td>
    <td>${app.appType.title}(${app.appType.name})</td>
  </tr>
  <tr>
    <td class="title">上下文地址</td>
    <td>${app.base}</td>
    <td class="title">入口</td>
    <td>${app.url}</td>
  </tr>
  <tr>
    <td class="title">导航风格</td>
    <td>${app.navStyle!}</td>
    <td class="title">logoUrl</td>
    <td>${app.logoUrl!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">引用资源</td>
    <td colspan="3">
    <style>.itable th, .itable td{padding:3px 5px;}</style>
    <table class="table table-sm table-mini" style="width:600px;">
      <thead>
        <th width="15%">数据源</th>
        <th width="15%">名称</th>
        <th width="15%">用户名</th>
        <th>密钥</th>
        <th width="15%">最大连接数</th>
      </thead>
      <tbody>
        [#list app.datasources as v]
          <tr>
            <td>${v.db.name}</td>
            <td align="center">${v.name}</td>
            <td align="center">${v.credential.username}</td>
            <td align="center">${v.credential.password}</td>
            <td align="center">${v.maximumPoolSize}</td>
          </tr>
        [/#list]
      </tbody>
    </table>
    </td>
  </tr>
</table>
[@b.foot/]
