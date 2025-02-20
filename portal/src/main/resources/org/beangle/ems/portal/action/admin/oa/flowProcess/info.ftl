[#ftl]
[@b.head/]
[@b.toolbar title="流程详情"]
  bar.addBack();
[/@]
<table class="infoTable">
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
    <td class="title">当前状态</td><td>${(process.status)!}</td>
  </tr>
  <tr>
    <td class="title">发起时间</td><td>${(process.startAt)?string("yy-MM-dd HH:mm")}~${(process.endAt?string("yy-MM-dd HH:mm"))!'--'}</td>
    <td class="title">业务KEY</td><td>${process.businessKey}</td>
  </tr>
  <tr>
    <td class="title">过程细节</td>
    <td colspan="3">
      <table class="table table-sm" style="width:85%">
        <thead>
          <tr>
            <th style="width:20%;">时间</th>
            <th style="width:20%;">步骤</th>
            <th style="width:20%;">受理人</th>
            <th style="width:40%;">备注</th>
          </tr>
        </thead>
        <tbody>
          [#list process.tasks?sort_by('startAt') as task]
            <tr">
              <td>${task.startAt?string("yy-MM-dd HH:mm")}</td>
              <td>${task.name}</td>
              <td>${(task.assignee.name)!}</td>
              <td>
                [#list task.comments as c]<div>${c.messages} <span class="text-muted">${c.updatedAt?string("yy-MM-dd HH:mm")}</span></div>[/#list]
              </td>
            </tr>
          [/#list]
        </tbody>
      </table>
    </td>
  </tr>
</table>
[@b.foot/]
