[@b.head "菜单管理"/]
[@b.toolbar title="菜单上传"]
  bar.addBack();
[/@]
[@b.form action="!importFormRemote" theme="list"]
   [@b.field label="远程地址"] <a href="${remoteMenuURL}" target="_blank">${remoteMenuURL}</a>[/@]
   [@b.field label="内容"]
     <div>
       [#if remoteResponse==200]
       <pre>${remoteContent?html}</pre>
       [#else]
       ${remoteResponse} 加载远程菜单资源失败.
       [/#if]
     </div>
   [/@]
   [@b.formfoot]
        <input type="hidden" name="menu.app.id" value="${Parameters['menu.app.id']}"/>
        [#if remoteResponse==200][@b.submit class="btn btn-primary" value="上传"  /][/#if]
   [/@]
[/@]
[@b.foot/]
