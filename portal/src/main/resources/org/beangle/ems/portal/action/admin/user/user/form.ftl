[#ftl]
[@b.head/]
[@b.toolbar title="修改用户"]bar.addBack("${b.text("action.back")}");[/@]
[@b.messages/]
[@b.tabs id="userinfotabs"]
  [@b.tab label="账户信息"]
    [@b.form name="userForm" action="!update" class="listform" theme="list"]
        [@b.textfield name="user.code" value=user.code style="width:200px;" required="true" maxlength="30"/]
        [@b.textfield name="user.name" value=user.name  style="width:200px;" required="true" maxlength="30"/]
        [@b.select name="user.category.id" label="身份" value=user.category! items=categories required="true"/]
        [@b.select items=departs label="部门" name="user.depart.id" value=user.depart! empty="..."/]
        [@b.select name="user.group.id" label="默认用户组" value=user.group! items =groups /]
        [@b.select name="group.id" label="附加用户组" values=userGroups items=groups multiple="true"/]
        [@b.cellphone name="user.mobile" value=user.mobile! /]
        [@b.email name="user.email" value=user.email! /]
        [@b.radios name="user.enabled" value=user.enabled items="1:action.activate,0:action.freeze"/]
        [@b.radios name="user.locked" value=user.locked items="1:锁定,0:解锁"/]
        [@b.password label="user.password" name="password" value="" maxlength="20" showStrength="true"/]
        [@b.startend label="有效期" name="user.beginOn,user.endOn" required="true,false" start=user.beginOn end=user.endOn format="date" comment="过期后不能登录"/]
        [@b.date name="user.passwdExpiredOn" value=user.passwdExpiredOn format="date"  required="true" comment="过期后，仍可登录，但需要及时更新密码。"/]
        [@b.textarea cols="50" rows="1" name="user.remark" value=user.remark! maxlength="50"/]
        [@b.formfoot]
          <input type="hidden" name="user.id" value="${user.id}" />
          [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
        [/@]
    [/@]
  [/@]
  [@b.tab label="角色"]
  <script type="text/javascript">
    function toggleMemberEnvAll(cb){
      var roleId = cb.getAttribute("data-role");
      var choices = document.getElementById("envChoices" + roleId);
      if (!choices) return;
      var allMode = cb.checked;
      choices.style.display = allMode ? "none" : "";
      var boxes = choices.querySelectorAll("input[type=checkbox]");
      for (var i = 0; i < boxes.length; i++) {
        if (allMode) boxes[i].checked = false;
      }
    }
    function toggleMemberEnvPanel(memberCb){
      var roleId = memberCb.getAttribute("data-role");
      var panel = document.getElementById("envPanel" + roleId);
      if (!panel) return;
      var show = memberCb.checked;
      panel.style.display = show ? "" : "none";
      if (!show) {
        var allCb = panel.querySelector("input.member-env-all");
        if (allCb) {
          allCb.checked = true;
          toggleMemberEnvAll(allCb);
        }
        var boxes = panel.querySelectorAll("input[name='env" + roleId + ".id']");
        for (var i = 0; i < boxes.length; i++) boxes[i].checked = false;
      }
    }
    function prepareMemberEnvSubmit(form){
      var memberBoxes = form.querySelectorAll("input.security_member");
      for (var i = 0; i < memberBoxes.length; i++) {
        toggleMemberEnvPanel(memberBoxes[i]);
      }
      var allBoxes = form.querySelectorAll("input.member-env-all");
      for (var i = 0; i < allBoxes.length; i++) {
        toggleMemberEnvAll(allBoxes[i]);
      }
      return true;
    }
  </script>
  <div class="container" style="margin-left:10px;">
  [@b.form name="memberForm" action="!saveRole" class="listform" theme="list" onsubmit="return prepareMemberEnvSubmit(this)"]
    [@b.grid items=roles?sort_by("indexno") var="role" sortable="false" theme="mini"]
      [@b.row]
        <tr [#if role??]id="${role.indexno}"[/#if]>
        [@b.col title="common.index" width="40px"]${role_index+1}[/@]
        [@b.treecol title="角色" property="name" width="400px"]
          <span [#if !role.enabled]class="ui-disabled" title="${b.text('action.freeze')}"[/#if]>
          ${role.indexno} ${role.name}[#if !role.enabled] (禁用)[/#if]
          </span>
        [/@]
        [@b.col title="成员" width="60px"]
          [#assign displayMember=(role.enabled && mngMemberMap.get(role).granter)/]
          [#assign isMemberChecked=(memberMap.get(role).member)!false /]
          <input type="checkbox" class="security_member" data-role="${role.id}" [#if !displayMember]style="display:none"[/#if] name="member${role.id}" [#if isMemberChecked]checked="checked"[/#if] onclick="toggleMemberEnvPanel(this)"/>
          [#if !displayMember && isMemberChecked]&radic;[/#if]
        [/@]
        [@b.col title="可授权" width="60px"]
          [#assign displayGranter=(role.enabled && mngMemberMap.get(role).granter)/]
          <input type="checkbox" name="granter${role.id}" [#if !displayGranter]style="display:none"[/#if] ${(memberMap.get(role).granter)?default(false)?string('checked="checked"','')}/>
          [#if !displayGranter && (memberMap.get(role).granter)!false]&radic;[/#if]
        [/@]
        [@b.col title="可管理" width="60px"]
          [#assign displayManager=(role.enabled && mngMemberMap.get(role).manager)/]
          <input type="checkbox" name="manager${role.id}" [#if !displayManager]style="display:none"[/#if] ${(memberMap.get(role).manager)?default(false)?string('checked="checked"','')}/>
          [#if !displayManager && (memberMap.get(role).manager)!false]&radic;[/#if]
        [/@]
        [@b.col title="业务场景" ]
          [#assign canEditEnv=(role.enabled && (mngMemberMap.get(role).granter || mngMemberMap.get(role).manager)) /]
          [#assign choiceEnvs=(roleChoiceEnvs.get(role.id?string))![] /]
          [#assign selectedEnvIds=(memberEnvIds.get(role.id?string))![] /]
          [#assign allEnvMode=(selectedEnvIds?size==0) /]
          [#if choiceEnvs?size==0]
            <span id="envPanel${role.id}" [#if !isMemberChecked]style="display:none"[/#if] class="text-muted">无可用场景</span>
          [#elseif choiceEnvs?size<=1]
            <span id="envPanel${role.id}" [#if !isMemberChecked]style="display:none"[/#if]>
              [#list choiceEnvs as env]${env.name}[#sep]、[/#list]
            </span>
          [#elseif canEditEnv]
            <span id="envPanel${role.id}" [#if !isMemberChecked]style="display:none"[/#if]>
              <label style="margin:0 8px 0 0;display:inline-block;white-space:nowrap;">
                <input type="checkbox" class="member-env-all" data-role="${role.id}" [#if allEnvMode]checked="checked"[/#if] onclick="toggleMemberEnvAll(this)"/> 不区分场景
              </label>
              <span id="envChoices${role.id}" [#if allEnvMode]style="display:none"[/#if]>
                [#list choiceEnvs as env]
                  [#assign envChecked=isMemberChecked && (!allEnvMode) && selectedEnvIds?seq_contains(env.id?string) /]
                  <label style="margin:0 8px 0 0;display:inline-block;white-space:nowrap;">
                    <input type="checkbox" name="env${role.id}.id" value="${env.id}" [#if envChecked]checked="checked"[/#if]/> ${env.name}
                  </label>
                [/#list]
              </span>
            </span>
          [#elseif isMemberChecked]
            [#if allEnvMode]不区分场景
            [#else]
              [#list choiceEnvs as env][#if selectedEnvIds?seq_contains(env.id?string)]${env.name}[#sep]、[/#if][/#list]
            [/#if]
          [/#if]
        [/@]
        </tr>
      [/@]
    [/@]
    [@b.formfoot]
       <input type="hidden" name="user.id" value="${user.id!}" />
        [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
  </div>
[/@]
  [#if user.persisted]
  [@b.tab label="全局数据权限" ]
  [@b.div href="/portal/admin/user/profile?forEdit=1&profile.user.id=${user.id}" /]
  [/@]
  [/#if]
[/@]
[@b.foot/]
