[#ftl]
[@b.head/]
[@b.toolbar title="新建/修改主题"]bar.addBack();[/@]
<style>form.listform label.title{width:120px;}</style>
[@b.tabs]
  [@b.form action=save_action theme="list" action=b.rest.save(theme)]
    [@b.textfield name="theme.name" label="名称" value="${theme.name!}" required="true" maxlength="200"/]
    [@b.textfield name="theme.primaryColor" label="超链接背景" value=theme.primaryColor! required="true" maxlength="10"/]
    [@b.textfield name="theme.navbarBgColor" label="导航区背景" value=theme.navbarBgColor! required="true" maxlength="10"/]
    [@b.textfield name="theme.searchBgColor" label="查询区背景" value=theme.searchBgColor! required="true" maxlength="10"/]
    [@b.textfield name="theme.gridbarBgColor" label="表格工具栏背景" value=theme.gridbarBgColor! required="true" maxlength="10"/]
    [@b.textfield name="theme.gridBorderColor" label="表格边框颜色" value=theme.gridBorderColor! required="true" maxlength="10"/]
    [@b.radios name="theme.enabled" label="是否启用" value=theme.enabled/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]