[@b.head "菜单管理"/]
  <div class="container" style="width:600px">
    <div>
      <h4>菜单管理 <small>菜单上传</small></h4>
    </div>
[@b.form action="!importFromXml"   enctype="multipart/form-data" class="form-inline" role="form"]
   <input type="hidden" name="menu.app.id" value="${Parameters['menu.app.id']}"/>
    <label for="zipfile" class="control-label">选择xml文件：</label>
    <div class="form-group">
      <input type="file" name="menufile"  id="menufile" class="form-control">
    </div>
    <div class="form-group">
        [@b.submit class="btn btn-primary" value="上传"/]
    </div>
[/@]
</div>
[@b.foot/]
