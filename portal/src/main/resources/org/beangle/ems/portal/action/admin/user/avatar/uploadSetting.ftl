[@b.head "照片管理"/]
[#include "nav.ftl"/]
  <div class="container-fluid">
[@b.form action="!upload"   enctype="multipart/form-data" class="form-inline" role="form"]
    <label for="zipfile" class="control-label">选择zip文件：</label>
    <div class="form-group">
      <input type="file" name="zipfile"  id="zipfile" class="form-control">
    </div>
    <div class="form-group">
        [@b.submit class="btn btn-primary" value="上传"/]
    </div>
[/@]
<hr>
</div>
[@b.foot/]
