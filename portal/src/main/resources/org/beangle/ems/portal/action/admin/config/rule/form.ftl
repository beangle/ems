[#ftl]
[@b.head/]
<style type="text/css">
  .paramEditInput{
    border:none;
    margin:1px;
  }
  .paramEditInputLight{
    margin:0;
    background-color:#E0ECFF;
    border:1px solid #AAA;
  }
</style>
[@b.toolbar title="添加/更新规则项目"]
  bar.addBack();
  function queryMeta(obj){
    var id = jQuery(obj).val();
    jQuery("#ruleName").val(jQuery(obj).find("option:selected").text());
    var res = jQuery.post("${b.url('!metaInfo')}.json",{metaId:id},function(){
      var meta = jQuery.parseJSON(res.responseText);
      jQuery("#ruleDescription").html(meta.description);
      jQuery("#ruleBusiness").html(meta.business);
      if(meta.params[0]){
        jQuery("#ruleParamsTable tbody").empty();
        var i = 0;
        while(meta.params[i]){
          var param = meta.params[i];
          jQuery("#ruleParamsTable tbody").html(jQuery("#ruleParamsTable tbody").html()+
            "<tr>"+
              "<input type='hidden' name='param" + i + ".id' value=''/>" +
              "<input type='hidden' name='param" + i + ".param.id' value='" + param.id + "'/>" +
              "<td>" + param.title + "</td>" +
              "<td>" + param.description + "</td>" +
              "<td><input type='text' name='param" + i + ".contents' value=''/></td>" +
            "</tr>"
          );
          i++;
        }
      }else{
        jQuery("#ruleParamsTable tbody").html(
          "<tr><td colspan='3'>没有可用参数</td></tr>"
        );
      }

    });
  }
[/@]
[@b.form action=b.rest.save(rule) theme="list"]
    [@b.field label="业务类型"]<span id="ruleBusiness">${rule.meta.business.name}</span>[/@]
    [@b.textfield label="规则名称" id="ruleName" required="true" maxLength="150px" name="rule.name" value=rule.name!/]
    [@b.textfield label="场景配置ID" required="true" maxLength="150px" name="rule.profileId" value=rule.profileId!/]
  [#if (rule.id)??]
    [@b.field label="规则描述"]${rule.meta.description}&nbsp;[/@]
    [@b.field label="规则名称"]${rule.meta.name}[/@]
    [#assign ruleParamsSize = rule.params?size/]
    [#assign paramMetas = rule.meta.params/]
  [#else]
    [#assign ruleParamsSize = 0/]
    [#if metas?size==0]
      [#assign paramMetas = []/]
    [#else]
      [#assign paramMetas = metas[0].params/]
    [/#if]
    [@b.select label="规则项目" name="rule.meta.id" items=metas required="true" onchange="queryMeta(this)"/]
    [@b.field label="规则描述"]<span id="ruleDescription">${(rule.meta.description)!}&nbsp;</span>[/@]
  [/#if]
  [@b.radios name="rule.enabled" label="是否有效" value=rule.enabled! /]
  [@b.field label="规则参数" required="true"]
    <table id="ruleParamsTable" class="table table-sm" style="width:85%">
      <thead>
        <tr>
          <th style="width:30%;">参数标题</th>
          <th style="width:40%;">参数描述</th>
          <th style="width:30%;">参数值</th>
        </tr>
      </thead>
      <tbody>
      [#assign paramIdx=0/]
      [#list paramMetas?sort_by('name') as meta]
        <tr>
          <td>${meta.title}</td>
          <td>${meta.description!}</td>
          <td>
            <input type="hidden" name="param${paramIdx}.meta.id" value="${meta.id}"/>
            [#assign finded=false/]
            [#list rule.params as p]
              [#if p.meta=meta][#assign paramValue=p.contents/][#assign finded=true/][#assign param=p/][#break/][/#if]
            [/#list]
            <input type="text" name="param${paramIdx}.contents" value="[#if finded]${param.contents}[/#if]" maxlength="80"/>
            <input type="hidden" name="param${paramIdx}.id" value="[#if finded]${param.id}[/#if]"/>
          </td>
        </tr>
        [#assign paramIdx=paramIdx+1/]
      [/#list]
      </tbody>
    </table>
  [/@]
  [@b.formfoot]
    [@b.reset/]
    [@b.submit/]
  [/@]
[/@]
