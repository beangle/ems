[#ftl]
<li>[#if tag.label??]<label for="${tag.id}" class="title">[#if (tag.required!"")=="true"]<em class="required">*</em>[/#if]${tag.label}:</label>[/#if]
[#assign selected=false/]
<select id="${tag.id}" [#if tag.title??]title="${tag.title}"[/#if] name="${tag.name}"${tag.parameterString}>
[#if tag.empty??]<option value="">${tag.empty}</option>[/#if][#rt/]
</select>[#if tag.comment??]<label class="comment">${tag.comment}</label>[/#if]
</li>

<script type="text/javascript">
  jQuery.get("${tag.uri}",function(datas){
    var select = $("#${tag.id}")
    for(var i in datas){
      var data = datas[i], value = data.code ? data.code : data.id
      select.append('<option value="'+value+'" title="'+data.name+'">'+data.name+'</option>');
    }
    [#if tag.value??]
    select.val("${tag.value}")
    [/#if]
  })
</script>
