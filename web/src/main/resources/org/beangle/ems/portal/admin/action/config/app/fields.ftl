[@b.textfield name="app.indexno" label="分类号" value="${app.indexno!}" required="true" maxlength="50"/]
[@b.textfield name="app.name" label="名称" value=app.name! required="true" maxlength="200"/]
[@b.textfield name="app.title" label="标题" value=app.title! required="true" maxlength="200"/]
[@b.textfield name="app.enTitle" label="英文标题" value=app.enTitle! required="true" maxlength="200"/]
[@b.select name="app.appType.id" label="类型" items=appTypes option="id,title" value=app.appType required="true" /]
[@b.select name="app.group.id" label="分组" value=app.group! option="id,title" required="true" items=groups?sort_by('indexno')/]
[@b.textfield name="app.base" label="上下文地址" value="${app.base!}" required="true" maxlength="200" style="width:300px"/]
[@b.textfield name="app.url" label="入口" value="${app.url!}" required="true" maxlength="200" style="width:300px"/]
[@b.textfield name="app.navStyle" label="导航风格" value=app.navStyle required="false" /]

[@b.field label="引用资源"]
  <div style="margin-left:120px;">
    <style>.itable th, .itable td{padding:3px 5px;}</style>
    <table border="1" class="formTable itable dstable">
      <thead>
        <th>数据源</th>
        <th style="width:60px">名称</th>
        <th style="width:100px">凭证</th>
        <th>最大连接数</th>
        <th>备注</th>
        <th>操作</th>
      </thead>
      <tbody>
        [#list app.datasources as v]
          <tr>
            <td>${v.db.name}</td>
            <td><input name="ds${v.db.id}.name" value="${v.name!}" style="width:60px" maxlength="40"/></td>
            <td>
              <select name="ds${v.db.id}.credential.id" style="width:100px">
                 [#list credentials as credential]
                 <option value="${credential.id}" [#if credential.id=v.credential.id]selected="selected"[/#if]>${credential.name}</option>
                 [/#list]
              </select>
            </td>
            <td>
               <input name="ds" type="hidden" value="${v.db.id}"/>
               <input name="ds${v.db.id}.db.id" type="hidden" value="${v.db.id}"/>
               <input class="maximumPoolSize" name="ds${v.db.id}.maximumPoolSize" value="${v.maximumPoolSize}" style="width:60px"/>
            </td>
            <td><input name="ds${v.db.id}.remark" value="${v.remark!}" style="width:100px"/></td>
            <td><button class="delDataSourceBtn btn btn-sm btn-danger"><i class="fas fa-minus"></i>删除</button></td>
           </tr>
        [/#list]
      </tbody>
    </table>
    <p><button class="addBtn btn btn-sm btn-info"><i class="fas fa-plus"></i>添加</button><p>
  </div>
[/@]
[@b.radios name="app.enabled" label="是否可用"  value=app.enabled required="true" /]
[@b.textfield name="app.secret" label="密钥" value="${app.secret!}" maxlength="200" required="true"/]
[@b.textarea name="app.remark" label="备注" value="${app.remark!}" maxlength="200"/]
[@b.formfoot]
  [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit" onsubmit="beforeSubmit"/]
[/@]
<script>
  $(".addBtn").click(function (){
    var w = 500;   //宽度
    var h = 600;   //高度
    var t = (screen.height-h)/2; //离顶部距离
    var l = (screen.width-w)/2; //离左边距离
    window.open("${b.url('!datasource')}","","top="+t+",left="+l+",height="+h+", width="+w+", modal=yes, titlebar=no, toolbar=no, menubar=no, scrollbars=yes, resizable=no,fullscreen=1, location=no,status=no");
    return false;
  });
  function addDataSource(datas){
    $.each(datas, function (index, value){
      var id = value[0], name = value[1];
        var tr = $('<tr><td>'+name+'</td>'+
            '<td><input name="ds" type="hidden" value="'+id+'"/><input name="ds'+id+'.db.id" type="hidden" value="' + id + '"/><input name="ds'+id+'.name" style="width:60px" maxlength="40"/></td>'+
            '<td><select name="ds'+id+'.credential.id" style="width:100px">[#list credentials as c]<option value="${c.id}">${c.name}</option>[/#list]</select></td>'+
            '<td><input class="maximumPoolSize" name="ds'+id+'.maximumPoolSize" style="width:60px"/></td>'+
            '<td><input name="ds'+id+'.remark" style="width:100px"/></td>'+
            '<td><button class="delDataSourceBtn btn btn-sm btn-danger"><i class="fas fa-minus"></i>删除</button></td></tr>');
        $(".dstable").append(tr);
        tr.hide().fadeIn();
    });
  }
  $(".dstable").on("click", ".delDataSourceBtn", function (){
    $(this).parent().parent().fadeOut(function (){$(this).remove()});
    return false;
  });

  function beforeSubmit(){
    var allNumber = true;
    $(".maximumPoolSize").each(function (){
      allNumber = allNumber && /^\d+$/.test(this.value);
    });
    if(!allNumber){
      alert("请在最大连接数内输入一个整数");
      return false;
    }
    return true;
  }
</script>
