[@b.head title="请选择一数据源"/]
<ol>
[#list dataSources as v]
<li><label><input type="checkbox" value="${v.id}"/>${v.name}</label></li>
[/#list]
</ol>
<p><button class="addBtn">添加</button></p>
<script>
  $(".addBtn").click(function (){
    var ipts = $("input:checked");
    if(ipts.length == 0){
      alert("请选择一个数据源！");
      return;
    }
    var datas = [];
    ipts.each(function (){
      datas.push([this.value, $(this).parent().text()]);
    });
    opener.addDataSource(datas);
    window.close();
  });
</script>
[@b.foot/]
