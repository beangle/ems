[#ftl]
[@b.head/]
<script type="text/javascript">
  beangle.load(["bui-tabletree"]);
</script>
[#assign labInfo][#if user.persisted]${b.text("action.modify")}[#else]${b.text("action.new")}[/#if] ${b.text("entity.user")}[/#assign]
[@b.toolbar title="修改用户角色"]bar.addBack("${b.text("action.back")}");[/@]
[@b.messages/]
[@b.tabs id="userinfotabs"]
  [@b.tab label="user.members"]
  [@b.form name="userForm" action="!save" class="listform" theme="list"]
    [@b.grid  items=roles?sort_by("indexno") var="role" sortable="false"]
      [@b.row]
        <tr [#if role??]id="${role.indexno}"[/#if]>
        [@b.col title="common.index" width="5%"]${role_index+1}[/@]
        [@b.treecol title="角色" property="name"]
          <span [#if !role.enabled]class="ui-disabled" title="${b.text('action.freeze')}"[/#if]>
          ${role.indexno} ${role.name}[#if !role.enabled] (禁用)[/#if]
          </span>
        [/@]
        [@b.col title="成员" width="10%"]
          [#assign displayMember=(role.enabled && mngMemberMap.get(role).granter)/]
          <input type="checkbox" class="security_member" [#if !displayMember]style="display:none"[/#if] name="member${role.id}" ${(memberMap.get(role).member)?default(false)?string('checked="checked"','')}/>
          [#if !displayMember && (memberMap.get(role).member)!false]&radic;[/#if]
        [/@]
        [@b.col title="可授权" width="10%"]
          [#assign displayGranter=(role.enabled && mngMemberMap.get(role).granter)/]
          <input type="checkbox" name="granter${role.id}" [#if !displayGranter]style="display:none"[/#if] ${(memberMap.get(role).granter)?default(false)?string('checked="checked"','')}/>
          [#if !displayGranter && (memberMap.get(role).granter)!false]&radic;[/#if]
        [/@]
        [@b.col title="可管理" width="10%"]
          [#assign displayManager=(role.enabled && mngMemberMap.get(role).manager)/]
          <input type="checkbox" name="manager${role.id}" [#if !displayManager]style="display:none"[/#if] ${(memberMap.get(role).manager)?default(false)?string('checked="checked"','')}/>
          [#if !displayManager && (memberMap.get(role).manager)!false]&radic;[/#if]
        [/@]
        [@b.col title="common.updatedAt" width="20%"]${(memberMap.get(role).updatedAt)!}[/@]
        </tr>
      [/@]
    [/@]
    [@b.formfoot]
       <input type="hidden" name="user.id" value="${user.id!}" />
        [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
      [/@]
  [/@]
[/@]
  [#if user.persisted]
  [@b.tab label="全局数据权限" ]
  [@b.div href="/admin/user/profile?forEdit=1&profile.user.id=${user.id}" /]
  [/@]
  [/#if]
[/@]
[@b.foot/]
