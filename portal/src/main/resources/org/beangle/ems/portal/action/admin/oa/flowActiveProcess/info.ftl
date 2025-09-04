[#ftl]
[@b.head/]
[@b.toolbar title="流程详情"]
  bar.addBack();
[/@]
<table class="table table-sm table-detail">
  <tr>
    <td class="title" width="15%">业务</td><td>${process.flow.business.name}</td>
    <td class="title" width="15%">流程</td><td>${process.flow.name}</td>
  </tr>
  <tr>
    <td class="title">业务步骤</td>
    <td colspan="3">
      [#list process.flow.activities?sort_by("idx") as act]${act.name}[#sep]&nbsp;[/#list]
    </td>
  </tr>
  <tr>
    <td class="title">发起人</td><td>${(process.initiator.code)!} ${(process.initiator.name)!}</td>
    <td class="title">当前状态</td><td>进行中</td>
  </tr>
  <tr>
    <td class="title">发起时间</td><td>${(process.startAt)?string("yy-MM-dd HH:mm")}</td>
    <td class="title">业务KEY</td><td>${process.businessKey}</td>
  </tr>
  <tr>
    <td class="title">当前过程</td>
    <td colspan="3">
      <table class="table table-sm table-mini" style="width:85%">
        <thead>
          <tr>
            <th style="width:20%;">时间</th>
            <th style="width:20%;">步骤</th>
            <th style="width:60%;">受理人</th>
          </tr>
        </thead>
        <tbody>
          [#list process.tasks?sort_by('startAt') as task]
            <tr">
              <td>${task.startAt?string("yy-MM-dd HH:mm")}</td>
              <td>${task.name}</td>
              <td>[#list task.assignees as u]${u.code} ${u.name}[#sep]&nbsp;[/#list]</td>
            </tr>
          [/#list]
        </tbody>
      </table>
    </td>
  </tr>
</table>
[@b.foot/]
