[@b.head "照片浏览"/]
  <div class="container ajax_container" style="width:100%">
    <div class="">
      <h2>照片管理 <small>照片浏览</small></h2>
    </div>
    <style>
      .imgdiv{width:88px; float:left; height:145px; overflow: hidden; margin:5px;}
      .imgdiv img{width:100%;}
      .imgdiv .text{text-align: center;}
    </style>
    <nav class="navbar navbar-default" role="navigation">
      <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
        [@b.form action="!index" name="photoSearchForm" id = "photoSearchForm" class="navbar-form navbar-left" role="form"]
        <div class="form-group ">
            <input type="text" name="user" value="${Parameters['user']!}" placeholder="用户号/姓名" class="form-control">
        </div>
          <input type="hidden" id="pageIndex" name="pageIndex" value="1">
          [@b.submit class="btn btn-primary" value="搜索"/]
        [/@]
        <div class="navbar-form navbar-right">
          [@b.a href="!uploadSetting" class="btn btn-default"]<span class="glyphicon glyphicon-plus"></span>上传照片[/@]
        </div>
      </div>
    </nav>
    [#if users?size>0]
      <div style="margin:-10px -5px 0 -5px;">
         [#list users as u]
            <div class="imgdiv">
              <img class="img-thumbnail" src="${b.url('!info?userId='+u.id)}"/>
              <div class="text">${u.code} ${u.name}</div>
            </div>
         [/#list]
        <div style="clear:both"></div>
      </div>
       <ul class="pagination pull-right">
         [#if users.pageIndex>1]
             <li><a href="#" onclick="gotoPage(${users.pageIndex-1})">&laquo;</a></li>
             [#if users.pageIndex > 1]
               <li><a onclick="gotoPage(1)">1</a></li>
             [/#if]
             [#if users.pageIndex > 3]
               <li><a>...</a></li>
             [/#if]
         [#else] <li class="disabled"><a href="#">&laquo;</a></li>
         [/#if]
         [#list (users.pageIndex - 3)..(users.pageIndex + 3) as p]
           [#if p > 0 && (p > 1 || users.pageIndex == 1) && p <= users.totalPages && (p < users.totalPages || users.pageIndex == users.totalPages)]
             <li class="[#if p == users.pageIndex]active[/#if]"><a onclick="gotoPage(${p})">${p}</a></li>
           [/#if]
         [/#list]
         [#if users.pageIndex != users.totalPages && users.totalPages > 0]
           [#if users.totalPages - users.pageIndex > 3]
             <li><a>...</a></li>
           [/#if]
           [#if users.pageIndex < users.totalPages]
             <li><a onclick="gotoPage(${users.totalPages})">${users.totalPages}</a></li>
           [/#if]
           <li><a onclick="gotoPage(${users.pageIndex+1})">&raquo;</a></li>
         [#else]
           <li class="disabled"><a href="#">&raquo;</a></li>
         [/#if]
       </ul>
       <script>
         function gotoPage(page){
           $("#pageIndex").val(page);
           bg.form.submit(document.photoSearchForm);
         }
       </script>
    [#else]
        <div class="jumbotron">
          <h1>没有找到图片</h1>
        </div>
    [/#if]
  </div>
</body>
</html>
