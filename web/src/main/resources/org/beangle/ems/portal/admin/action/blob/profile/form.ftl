[#ftl]
[@b.head/]
[@b.toolbar title="新建/修改业务文件配置"]bar.addBack();[/@]
[@b.form action=b.rest.save(profile) title="修改业务文件配置" theme="list"]
  [@b.textfield name="profile.name" required="true" label="common.name" value="${profile.name!}" maxlength="50" style="width:250px"/]
  [@b.textfield name="profile.base" required="true" label="路径" value="${profile.base!}" maxlength="50"  comment="路径唯一"/]
  [@b.radios label="按照sha命名" name="profile.namedBySha"  items="1:是,0:否" value= profile.namedBySha /]
  [@b.radios label="文件公开下载" name="profile.publicDownload" value=profile.publicDownload  items="1:是,0:否"/]
  [@b.select2 label="可写应用" name1st="alternativeApp.id" name2nd="app.id"
       items1st=alternativeApps items2nd= profileApps
       option="id,title"/]
  [@b.formfoot]
    [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
  [/@]
[/@]
[@b.foot/]
