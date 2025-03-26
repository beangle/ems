[@b.head/]
[@b.toolbar title='用户签名信息']
[/@]
<div class="search-container">
    <div class="search-panel">
      [@b.form name="signatureForm" action="!search" title="ui.searchForm" target="proposalList" theme="search"]
        [@b.textfield name="signature.user.code" label="账户" maxlength="30000"/]
        [@b.textfield name="signature.user.name" label="姓名"/]
        [@b.select name="signature.user.department.id" label="部门" items=departs/]
      [/@]
    </div>
    <div class="search-list">[@b.div id="proposalList" href="!search"/]</div>
</div>
[@b.foot/]
