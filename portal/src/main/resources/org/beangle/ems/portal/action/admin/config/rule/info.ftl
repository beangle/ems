[#ftl]
[@b.head/]
[@b.toolbar title="规则配置"]
  bar.addBack();
[/@]
<table class="table table-sm table-detail">
  <tr>
    <td class="title" width="15%">业务</td><td>${rule.meta.business.name}</td>
    <td class="title" width="15%">名称</td><td>${rule.name}</td>
  </tr>
  <tr>
    <td class="title">描述</td><td>${rule.meta.description}</td>
    <td class="title">是否启用</td><td>${rule.enabled?string('是','否')}</td>
  </tr>
  <tr>
    <td class="title">参数</td>
    <td colspan="3">
      <table class="table table-sm table-mini" style="width:85%">
        <thead>
          <tr>
            <th style="width:30%;">参数标题</th>
            <th style="width:40%;">参数描述</th>
            <th style="width:30%;">参数值</th>
          </tr>
        </thead>
        <tbody>
          [#list rule.params?sort_by(['meta','name']) as param]
            <tr">
              <td>${param.meta.title}</td>
              <td>${param.meta.description}</td>
              <td>${param.contents}</td>
            </tr>
          [/#list]
        </tbody>
      </table>
    </td>
  </tr>
</table>
[@b.foot/]
