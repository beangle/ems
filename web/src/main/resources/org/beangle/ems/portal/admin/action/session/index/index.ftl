[@b.head/]
<div class="container-fluid">
  [#assign sessionCaption]在线记录 ${sessionInfoes.totalItems}(每5秒自动刷新)[/#assign]
  [@b.grid items=sessionInfoes var="s" caption=sessionCaption refresh="5"]
    [@b.row]
      [@b.col title="序号" width="4%"]${s_index+1}[/@]
      [@b.col title="用户名" width="14%" property="principal"/]
      [@b.col title="姓名" width="14%" property="description"/]
      [@b.col title="分类" width="5%" property="category.name" sortable="false"/]
      [@b.col title="IP" width="14%" property="ip"/]
      [@b.col title="客户端" width="17%" property="agent"/]
      [@b.col title="操作系统" width="12%" property="os"/]
      [@b.col title="登录时间" width="10%" property="login_at"]${s.loginAt?string('MM-dd HH:mm:ss')}[/@]
      [@b.col title="最后访问时间" width="10%" property="last_access_at"]${s.lastAccessAt?string('MM-dd HH:mm:ss')}[/@]
    [/@]
  [/@]
  <br/>
</div>
[@b.foot/]
