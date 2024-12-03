[#ftl]
[@b.head/]
<style>
  #paramsEditTr input[type=text] {
    border:1px solid #AAA;
    width:100%;
  }
  #paramsEditTr input[type=text]:first-of-type {
    border:1px solid #AAA;
    width:80%;
  }
  .paramEditInput{
    border:1px solid #AAA;
    width:100%;
  }
  .paramEditInput:first-of-type{
    border:1px solid #AAA;
    width:80%;
  }
  .paramEditInput:hover{
    background-color:#E0ECFF;
  }
</style>

[@b.toolbar title="添加/更新审核规则"]
  bar.addBack();
[/@]
[@b.form action=b.rest.save(ruleMeta) theme="list" name="ruleForm"]
  [@b.select name="ruleMeta.business.id" label="业务类型" value=ruleMeta.business! items=businesses required="true"/]
  [@b.textfield name="ruleMeta.name" title="规则名称" value=ruleMeta.name! maxlength="100" required="true"/]
  [@b.textfield name="ruleMeta.title" label="规则标题" value=ruleMeta.title! maxlength="80" required="true"/]
  [@b.textfield name="ruleMeta.description" label="规则描述" value=(ruleMeta.description)! maxlength="200" required="true" comment="(限200字)"/]
  [@b.field label="规则参数"]
    <table class="rulesTable" width="600px">
      <thead>
        <th style="width:20%">参数名称</th>
        <th style="width:20%">参数类型</th>
        <th style="width:20%">参数标题</th>
        <th style="width:35%">参数描述</th>
        <th style="width:5%;"></th>
      </thead>
      <tbody>
        [@ruleParams params=ruleMeta.params/]
        [#list ruleMeta.params?size .. 5 as paramCount]
        <tr id="paramsEditTr">
          <td><input id="param_name" name="${paramCount}.name" type="text" value="" maxlength="80" style="width:80px"/></td>
          <td>
            <input id="param_type" type="text" name="${paramCount}.dataType" value="" maxlength="80" />
          </td>
          <td><input id="param_title" type="text" name="${paramCount}.title" value="" maxlength="80" /></td>
          <td><input id="param_description" type="text" name="${paramCount}.description" value="" maxlength="80" /></td>
        </tr>
        [/#list]
      </tbody>
    </table>
  [/@]
  [@b.formfoot]
    [@b.submit/]  [@b.reset/]
  [/@]
[/@]

[#macro ruleParams params]
  [#if params?? && params?size>0]
    [#list params?sort_by("name") as param]
      [#assign paramJSid = param_index/]
      <tr>
        <td>
          <input type="text" class="paramEditInput" name="${paramJSid}.name" value="${param.name}" title='参数名称' maxlength="80" />
        </td>
        <td >
          <input type="text" class="paramEditInput" name="${paramJSid}.dataType" value="${param.dataType}" title='参数类型' maxlength="80" />
        </td>
        <td >
          <input type="text" class="paramEditInput" name="${paramJSid}.title" value="${param.title}" title='参数标题' maxlength="80" />
        </td>
        <td >
          <input type="text" class="paramEditInput" name="${paramJSid}.description" value="${param.description}" title='参数描述' maxlength="80" />
        </td>
      </tr>
    [/#list]
  [/#if]
[/#macro]
