[@b.head "照片管理"/]
    <style>
      .imgdiv{width:88px; float:left; height:145px; overflow: hidden; margin:5px;}
    </style>
  <div class="container">
    <div>
      <h1>个人照片 <small>照片上传</small></h1>
    </div>
    [#assign user = users?first/]
      <img class="img-thumbnail" style="height:145px;" src="${avatar_url}"/>${user.name}(${user.code})
[@b.form action="!upload" enctype="multipart/form-data" class="form-inline" role="form"]
    <label for="photo" class="control-label">选择照片文件：</label>
    <div class="form-group">
      <input type="file" name="photo"  id="photo" class="form-control">
    </div>
    <div class="form-group">
        [@b.submit class="btn btn-primary" value="上传"/]
    </div>
[/@]
</div>
[@b.foot/]
