[@b.grid items=signatures var="signature"]
  [@b.gridbar]
    bar.addItem("上传",action.add());
    bar.addItem("修改",action.edit());
    var m =bar.addMenu("下载",action.multi("download",null,null,"_blank"));
    m.addItem("从其他数据源中同步",action.method('uploadDbSetting'));
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="user.code"  title="账号" width="100px"/]
    [@b.col property="user.name"  title="姓名" width="130px"/]
    [@b.col property="user.depart.name"  title="部门" width="200px"/]
    [@b.col property="mediaType"  title="类型" width="130px"/]
    [@b.col property="signature"  title="大小" width="130px"]
      ${(signature.fileSize/1024.0)?string(".##")}K
    [/@]
    [@b.col title="签名"]
      <img src="${paths.get(signature)}" height="30px"/>
    [/@]
    [@b.col property="updatedAt" title="更新时间" width="130px"]
      ${signature.updatedAt?string('yy-MM-dd HH:mm')}
    [/@]
  [/@]
[/@]
