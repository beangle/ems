[#ftl]
[@b.head/]
[@b.toolbar title="数据配置项"]
  bar.addItem("${b.text('action.new')}","add()");
[/@]
[@b.messages slash="3"/]
[#if (profiles?size==0)]没有设置[/#if]
<div style="width:100%;display:flex">
 [#list profiles as profile]
 [@b.card style="width:50%"]
   [@b.card_header title=profile.name]
     [@b.card_tools]
       [@b.a href="!edit?id=" + profile.id class="btn btn-tool"]<i class="fa fa-edit"></i>修改[/@]
       [@b.a onclick="removeProfile(${profile.id});return false;" class="btn btn-tool"]<span class="text-danger"><i class="fa fa-times"></i>删除</span>[/@]
     [/@]
     [@b.card_body]
        <div class="table-responsive">
          <table class="table no-margin m-0 compact">
            <tbody>
             [#list profile.properties?keys as field]
            <tr>
              <td>${field.title}</td>
              <td>
                [#if profile.properties.get(field)??]
                  [#if !field.valueType && field.properties?? && profile.properties.get(field)!='*']
                    [#list fieldMaps[profile.id?string][field.name]! as value][#list field.properties?split(",") as pName]${value[pName]!} [/#list][#if value_has_next],[/#if][/#list]
                  [#else]
                    [#if fieldMaps[profile.id?string][field.name]?is_collection]
                      [#list fieldMaps[profile.id?string][field.name] as d]${d}[#if d_has_next],[/#if][/#list]
                    [#else]
                      ${fieldMaps[profile.id?string][field.name]}
                    [/#if]
                  [/#if]
                [/#if]
              </td>
            </tr>
            [/#list]
            </tbody>
          </table>
        </div>
     [/@]
   [/@]
 [/@]
[/#list]
</div>
<br/>
[@b.form name="profileForm" action="!remove"/]
<script type="text/javascript">
  function add(){
    var form = document.profileForm;
    form.action="${b.url('!editNew?profile.user.id='+Parameters['profile.user.id'])}";
    bg.form.submit(form);
  }
  function removeProfile(profileId){
    if(!confirm("确定删除?")) return false;
    var form =document.profileForm;
    bg.form.addInput(form,"profile.id",profileId);
    form.action="${b.url('!remove?_method=delete')}";
    return bg.form.submit(form);
  }
</script>
[@b.foot/]
