[@b.head "照片浏览"/]
  <div class="container ajax_container" style="width:100%">
    <style>
      .imgdiv{width:88px; float:left; height:145px; overflow: hidden; margin:5px;}
      .imgdiv img{width:100%;}
      .imgdiv .text{text-align: center;}
    </style>
    <nav role="navigation" class="navbar navbar-expand-lg navbar-light">
      <a class="navbar-brand" href="#">照片管理</a>
      <div class="collapse navbar-collapse">
        <ul class="navbar-nav mr-auto mt-2 mt-lg-0">
          <li class="nav-item active"><a class="nav-link" href="#">照片浏览</a></li>
        </ul>
        [@b.form action="!index" name="photoSearchForm" id="photoSearchForm" class="form-inline"]
          <div class="form-group">
            <input type="text" name="user" value="" placeholder="用户号/姓名" class="form-control form-control-sm">
          </div>
          <input type="hidden" id="pageIndex" name="pageIndex" value="1">
          <input type="submit" onclick="bg.form.submit('photoSearchForm',null,null,null);return false;" class="btn btn-sm btn-outline-primary" value="搜索">
        [/@]
        [@b.a href="!uploadSetting" class="btn btn-sm btn-default"]<i class="fa-solid fa-upload"></i></span>上传照片[/@]
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
       <ul class="pagination float-right">
         [#if users.pageIndex>1]
             <li class="page-item"><a href="#" onclick="gotoPage(${users.pageIndex-1})" class="page-link">&laquo;</a></li>
             [#if users.pageIndex > 1]
               <li class="page-item"><a class="page-link" onclick="gotoPage(1)">1</a></li>
             [/#if]
             [#if users.pageIndex > 3]
               <li><a>...</a></li>
             [/#if]
         [#else] <li class="disabled" class="page-item"><a href="#" class="page-link">&laquo;</a></li>
         [/#if]
         [#list (users.pageIndex - 3)..(users.pageIndex + 3) as p]
           [#if p > 0 && (p > 1 || users.pageIndex == 1) && p <= users.totalPages && (p < users.totalPages || users.pageIndex == users.totalPages)]
             <li class="page-item [#if p == users.pageIndex]active[/#if]"><a class="page-link" onclick="gotoPage(${p})">${p}</a></li>
           [/#if]
         [/#list]
         [#if users.pageIndex != users.totalPages && users.totalPages > 0]
           [#if users.totalPages - users.pageIndex > 3]
             <li class="page-item"><a class="page-link">...</a></li>
           [/#if]
           [#if users.pageIndex < users.totalPages]
             <li class="page-item"><a class="page-link" onclick="gotoPage(${users.totalPages})">${users.totalPages}</a></li>
           [/#if]
           <li class="page-item"><a class="page-link" onclick="gotoPage(${users.pageIndex+1})">&raquo;</a></li>
         [#else]
           <li class="page-item disabled"><a class="page-link" href="#">&raquo;</a></li>
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
