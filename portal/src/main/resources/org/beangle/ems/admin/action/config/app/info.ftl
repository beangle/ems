[#ftl]
[@b.head/]
[@b.toolbar title="应用信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">名称</td>
    <td class="content">${app.name}</td>
    <td class="title" width="20%">标题</td>
    <td class="content">${app.indexno} ${app.title}</td>
  </tr>
  <tr>
    <td class="title">领域</td>
    <td class="content">${app.domain.title}(${app.domain.name})</td>
    <td class="title">类型</td>
    <td class="content">${app.appType.title}(${app.appType.name})</td>
  </tr>
  <tr>
    <td class="title">上下文地址</td>
    <td class="content">${app.base}</td>
    <td class="title">入口</td>
    <td class="content">${app.url}</td>
  </tr>
  <tr>
    <td class="title">导航风格</td>
    <td class="content">${app.navStyle!}</td>
    <td class="title">logoUrl</td>
    <td class="content">${app.logoUrl!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">引用资源</td>
    <td class="content" colspan="3">
    <style>.itable th, .itable td{padding:3px 5px;}</style>
    <table border="1" class="itable dstable">
      <thead>
        <th>数据源</th>
        <th>名称</th>
        <th>用户名</th>
        <th>密钥</th>
        <th>最大连接数</th>
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
