[#ftl]
[@b.head/]
[@b.grid items=dbs var="db"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("连接测试")}",action.single("testSetting"));
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="name" title="名称"][@b.a href="!info?id=${db.id}"]${db.name!}[/@][/@]
    [@b.col width="10%" property="driver" title="驱动"]${db.driver!}[/@]
    [@b.col width="15%" property="serverName" title="IP"]${db.serverName!}[/@]
    [@b.col width="15%" property="databaseName" title="数据库名"]${db.databaseName!}[/@]
    [@b.col width="10%" property="portNumber" title="端口"]${db.portNumber!}[/@]
    [@b.col width="30%" property="url" title="URL"]${db.url!}[/@]
  [/@]
[/@]
[@b.foot/]
