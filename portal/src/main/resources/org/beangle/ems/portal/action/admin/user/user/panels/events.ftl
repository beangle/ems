[#ftl]
<div class="card card-primary card-outline">
  <div class="card-header"><h3 class="card-title">最近的历史登录退出信息</h3></div>
  <div class="card-body">
  [#assign eventTypeNames={'Login':'登录','Logout':'退出'}/]
  [#if (sessionEvents?size==0)]没有登录过系统[#else]
   <table class="table table-sm table-striped">
     <thead>
       <tr>
         <th>时间</th>
         <th>类型</th>
         <th>内容</th>
       </tr>
     </thead>
     <tbody>
       [#list sessionEvents as e]
       <tr>
         <td>${e.updatedAt?string("MM-dd HH:mm")}</td>
         <td>${eventTypeNames[e.eventType?string]!(e.eventType.name)}</td>
         <td>${e.detail!}</td>
       </tr>
       [/#list]
     </tbody>
   </table>
  [/#if]
  </div>
</div>
