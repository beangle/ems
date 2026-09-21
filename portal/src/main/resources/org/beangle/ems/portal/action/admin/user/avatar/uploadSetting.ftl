[@b.head "照片管理"/]
[#include "nav.ftl"/]
  <div class="container-fluid">
[@b.form action="!upload"   enctype="multipart/form-data" class="d-flex flex-wrap align-items-center gap-2" role="form"]
    <label for="zipfile" class="control-label">选择zip文件：</label>
    <div class="mb-3">
      <input type="file" name="zipfile"  id="zipfile" class="form-control">
    </div>
    <div class="mb-3">
        [@b.submit class="btn btn-primary" value="上传"/]
    </div>
[/@]
<hr>
</div>
[@b.foot/]
