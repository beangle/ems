[#ftl]
<div class="card card-primary card-outline">
  <div class="card-header"><h3 class="card-title">在线记录</h3></div>
  <div class="card-body">
  [#if (sessioninfoes?size==0)]没有在线[#else]
   <table class="table table-sm">
     <thead>
       <tr>
         <th>登录时间</th>
         <th>最后访问</th>
         <th>IP</th>
         <th>客户端</th>
       </tr>
     </thead>
     <tbody>
       [#list sessioninfoes as activity]
       <tr>
         <td>${activity.loginAt?string("MM-dd HH:mm")}</td>
         <td>${activity.lastAccessAt?string("MM-dd HH:mm")}</td>
         <td>${activity.ip!('')}</td>
         <td>${activity.agent!}/${activity.os!('')}</td>
       </tr>
       [/#list]
     </tbody>
   </table>
   [/#if]
   </div>
  </div>
