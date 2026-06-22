[@b.head title="首页"]
  ${b.static.load(["ems-shell"])}
[/@]
[#macro displayFrame mainHref=""]
<div class="wrapper">
    <nav id="main_header" class="main-header navbar navbar-expand navbar-dark border-bottom-0" style="background-color:var(--navbar-bg-color)">
      <ul class="nav navbar-nav">
         <li class="nav-item">
            <a class="nav-link" data-ems-pushmenu title="隐藏/显示菜单" href="#" role="button"><i class="fas fa-bars"></i></a>
         </li>
      </ul>
      <ul class="nav navbar-nav" id="top_nav_bar"></ul>

      <ul class="navbar-nav ml-auto" id="navbar-setting">
        <li class="nav-item dropdown">
          <a href="#" class="nav-link" data-toggle="dropdown">
            <i class="far fa-comments"></i>
            <span class="badge badge-danger navbar-badge" id="newly-message-count">0</span>
          </a>
          <div id="newly-message" class="dropdown-menu dropdown-menu-lg dropdown-menu-right" style="left: inherit; right: 0px;min-width:280px"></div>
        </li>

        <li class="nav-item dropdown tasks-menu">
          <a href="#" class="nav-link" data-toggle="dropdown">
            <i class="far fa-flag"></i>
            <span class="badge badge-warning navbar-badge" id="newly-task-count">0</span>
          </a>
          <div id="newly-task" class="dropdown-menu dropdown-menu-lg dropdown-menu-right" style="left: inherit; right: 0px;min-width:280px"></div>
        </li>

        <li class="nav-item">
          <a class="nav-link" data-ems-fullscreen href="#" role="button">
          <i class="fas fa-expand-arrows-alt"></i>
          </a>
        </li>
        <li class="nav-item dropdown user user-menu" style="margin-right: -5px;">
          <a href="#" class="nav-link" data-toggle="dropdown" title="${nav.principal.description}" style="padding-left: 5px;padding-right: 0px;">
            <img src="${nav.avatarUrl}" class="user-image">
          </a>
          <ul class="dropdown-menu">
            <li class="user-header">
              <img src="${nav.avatarUrl}" class="img-circle" alt="User Image">
              <p>
                ${nav.principal.description} - (${nav.principal.name})
                [#if nav.username != nav.principal.name]<small> 模拟${nav.username} [@b.a href="!index" onclick="removeRunAs()"]退出模拟[/@]</small>[/#if]
                <small>[#if nav.principal.remoteToken??]统一身份平台登录[#else]本地登录[/#if]</small>
                [#if nav.username != nav.principal.name]
                <script>
                  function removeRunAs(){
                    beangle.cookie.remove("beangle.security.runAs","/");
                  }
                </script>
                [/#if]
              </p>
            </li>
            <li class="user-footer">
              [#if !nav.principal.credentialReadOnly]
              <div class="float-sm-left">
                <a href="/cas/edit" class="btn btn-default btn-flat"><i class="nav-icon far fa-user"></i>修改密码</a>
              </div>
              [/#if]
              <div class="float-sm-right">
                <a href="${b.url('!logout')}" onclick="emsShell.clearNavState();return true;" class="btn btn-default btn-flat" target="_top">
                  <i class="nav-icon fa fa-door-open"></i>退出&nbsp;&nbsp;
                </a>
              </div>
            </li>
          </ul>
        </li>
        <li class="nav-item">
          <a href="#" style="padding:0.3125rem 0.35rem" class="nav-link" data-ems-control-sidebar><i class="fa fa-cog"></i></a>
        </li>
      </ul>
    </nav>

  <aside id="main_siderbar" class="main-sidebar sidebar-light-lightblue elevation-4" style="font-size:0.875rem;overflow: hidden;">
    <a href="/portal" class="brand-link" title="${nav.org.name} ${nav.domain.title}" style="border:0px;background-color:var(--navbar-bg-color)" onclick="emsShell.clearNavState();return true;">
      <img src="${nav.domain.logoUrl!}" class="brand-image" style="margin-left: 0rem;"/>
      <span class="brand-text font-weight-light" id="appName" style="font-size: 1rem;color: rgba(255,255,255,.8);"></span>
    </a>
    <div class="form-inline" style="display:none">
      <div class="input-group">
        <input class="form-control form-control-sidebar" type="search" placeholder="Search" aria-label="Search" id="menu_searcher">
        <div class="input-group-append">
          <button type="button" class="btn btn-sidebar">
            <i class="fas fa-search fa-fw"></i>
          </button>
        </div>
      </div>
      <div class="sidebar-search-results"><div class="list-group"></div></div>
    </div>
    <div class="sidebar" style="padding-right:0px">
      <nav class="mt-2">
        <ul id="menu_ul" class="nav nav-pills nav-sidebar flex-column nav-child-indent ems-sidebar-menu" role="menu"></ul>
      </nav>
    </div>
  </aside>
  <div class="content-wrapper" id="main_wrapper">
    [@b.div id="main"/]
  </div>

  <aside id="control_sidebar" class="control-sidebar control-sidebar-light">
    <ul class="nav nav-tabs nav-justified control-sidebar-tabs">
      <li class="nav-item"><a class="nav-link active" style="padding: .4rem .8rem;" href="#control-sidebar-theme-options-tab" data-toggle="tab" aria-expanded="true"><i class="fa fa-wrench"></i></a></li>
      <li class="nav-item"><a class="nav-link" style="padding: .4rem .8rem;" href="#control-sidebar-home-tab" data-toggle="tab" aria-expanded="false"><i class="fa fa-home"></i></a></li>
    </ul>
    <div class="tab-content">
      <div id="control-sidebar-theme-options-tab" class="tab-pane active" style="padding: 10px 15px;">
        <h6 class="control-sidebar-heading">布局选项</h6>
        <div class="form-group">
          <div class="mb-2"><input type="checkbox" id="sticky_header"><label for="sticky_header">固定头部导航</label></div>
          <div class="mb-2"><input type="checkbox" id="nav_multi_tab"><label for="nav_multi_tab">工作台多标签</label></div>
          <p class="small text-muted mb-2" style="margin-top:-0.25rem;">关闭后为单槽模式；变更后请刷新页面生效。</p>
          <div class="mb-2">
            导航风格:
            <input name="nav_siderbar_theme" value="light" checked="checked" id="nav_siderbar_theme_light" type="radio" onclick="emsShell.changeNavSidebarTheme(this.value)">
              <label for="nav_siderbar_theme_light">浅白</label>
            <input name="nav_siderbar_theme" value="dark" id="nav_siderbar_theme_dark" type="radio" onclick="emsShell.changeNavSidebarTheme(this.value)">
              <label for="nav_siderbar_theme_dark">暗黑</label>
          </div>
          <div class="mb-2">
            界面语言:
            <input name="request_locale" value="zh_CN" type="radio" [#if !locale?string?contains('en')] checked="checked"[/#if]  id="local_zh" onclick="changeLocale(this.value)">
              <label for="local_zh">中文</label>
            <input name="request_locale" value="en_US" type="radio" [#if locale?string?contains('en')] checked="checked"[/#if] id="local_en" onclick="changeLocale(this.value)">
              <label for="local_en">英文</label>
            <script>
               function changeLocale(locale){
                  this.location=("${b.url('!index')}"+"?request_locale="+locale);
               }
            </script>
          </div>
          <div class="mb-2">
            字体大小:
            <input name="root_font_size" value="0.9286em" id="root_font_size_small" type="radio" ><label for="root_font_size_small">小</label>
            <input name="root_font_size" value="1em" checked="checked" id="root_font_size_middle" type="radio"><label for="root_font_size_middle">中</label>
            <input name="root_font_size" value="1.07143em" id="root_font_size_large" type="radio" ><label for="root_font_size_large">大</label>
          </div>
          <div class="mb-2">
            每页数据量<select id="page_size_selector">
              [#list [10,20,30,50,70,100,200,500] as ps]
              <option value="${ps}" [#if ps==20]selected[/#if]>${ps}</option>
              [/#list]
            </select>
          </div>
          <hr/>
          <div class="mb-2">
            <ul style="padding-left: 0px;list-style-type: none;">
              <li class="mb-2">主要字体颜色：<input type="color" id="theme_primaryColor" onchange="changeTheme()" style="height: 20px;padding: 0px;" value=""/></li>
              <li class="mb-2">导航区背景：<input type="color" id="theme_navbarBgColor" onchange="changeTheme()" style="height: 20px;padding: 0px;" value=""/></li>
              <li class="mb-2">查询区背景：<input type="color" id="theme_searchBgColor" onchange="changeTheme()" style="height: 20px;padding: 0px;"  value=""/></li>
              <li class="mb-2">工具栏背景：<input type="color" id="theme_gridbarBgColor" onchange="changeTheme()" style="height: 20px;padding: 0px;"  value=""/></li>
              <li class="mb-2">表格边框颜色：<input type="color" id="theme_gridBorderColor" onchange="changeTheme()" style="height: 20px;padding: 0px;"  value=""/></li>
              <li><button class="btn btn-outline-primary btn-sm" onclick="emsShell.changeTheme(null,true)">恢复默认值</button>
            </ul>
          </div>
        </div>
      </div>
      <div class="tab-pane" id="control-sidebar-home-tab" style="padding: 10px 15px;">
        <h6 class="control-sidebar-heading">近期活动</h6>
        <ul class="control-sidebar-menu">
          <li>时间 <div id="clock" style="display:inline"></div></li>
        </ul>
      </div>
    </div>
  </aside>
  <div class="control-sidebar-bg"></div>
</div>
<script type="text/javascript">
  beangle.require(["wujie","ems-shell"], function (wujie, emsShell) {
    if (wujie && typeof wujie.startApp === "function") {
      window.wujie = wujie;
    } else if (emsShell.ensureWujieRuntime) {
      emsShell.ensureWujieRuntime();
    }
    emsShell.config.api='${nav.ems.api}';
    var app = {'name':'${nav.app.name}',"title":'${nav.domain.title}','base':'${nav.app.base}'}
    var portal = {'name':'${nav.app.name}',"title":'${nav.domain.title}','base':'${nav.app.base}'}
    var params={}
    [#list nav.params as k,v]
    params['${k}']='${v}';
    [/#list]
    if (!params['wujieIframeSrc']) {
      params['wujieIframeSrc'] = '${b.base}/index?about=1';
    }
    [#-- multiTab：服务端 nav.params 可传；否则读 localStorage beangle.ems.multi_tab；再默认多标签。侧栏「布局选项」可改本地偏好。 --]
    [#if Parameters['group.id']?? && Parameters['group.id']?length>0]
    params['initialGroupId']='${Parameters['group.id']}';
    [/#if]
    [#if nav.profiles??]
    emsShell.init(${nav.profiles},${nav.cookie!'null'});
    if(emsShell.config.profiles.length>0 && emsShell.config.profile){
      var default_p = emsShell.config.profile
      for(var i in default_p){
        if(i != "id") params[i] = default_p[i];
      }
      params['maxTopItem']=8;
    }
    [/#if]
    params['multiTab'] = emsShell.resolveMultiTabParam(params['multiTab']);

    jQuery(document).ready(function(){
      /* 左侧分组由 restoreNav 阶段二渲染（结合快照/hash/group.id），此处不预先 displayFirstGroup */
      emsShell.createNav(app,portal,${nav.menusJson},params,false);
      [#if nav.profiles??]
      emsShell.createProfileNav();
      [/#if]
      [#if mainHref?? && mainHref?length>0 ]
      emsShell.setWelcomeUrl('${b.url(mainHref)}');
      [/#if]
      var theme={"primaryColor": "${nav.theme.primaryColor}","navbarBgColor": "${nav.theme.navbarBgColor}", "searchBgColor": "${nav.theme.searchBgColor}", "gridbarBgColor": "${nav.theme.gridbarBgColor}", "gridBorderColor": "${nav.theme.gridBorderColor}"}
      emsShell.setup(theme,params);
      emsShell.enableSearch('menu_searcher');
    });
  });

  var clockOffset=0;
  $.get("${nav.ems.api}/tools/sys/time/now",function(data,status){
     clockOffset = parseInt(data)-(new Date()).getTime();
     setInterval(showTime, 1000);
  });

  function showTime() {
      let time = new Date();
      time.setTime(time.getTime()+clockOffset);
      let hour = time.getHours();
      let min = time.getMinutes();
      let sec = time.getSeconds();
      hour = hour < 10 ? "0" + hour : hour;
      min = min < 10 ? "0" + min : min;
      sec = sec < 10 ? "0" + sec : sec;

      document.getElementById("clock").innerHTML = (hour + ":" + min + ":" + sec);
  }

  function changeTheme(i){
    if(!i) i=20;
    var theme={}
    theme.primaryColor=jQuery("#theme_primaryColor").val();
    theme.navbarBgColor=jQuery("#theme_navbarBgColor").val();
    theme.searchBgColor=jQuery("#theme_searchBgColor").val();
    theme.gridbarBgColor=jQuery("#theme_gridbarBgColor").val();
    theme.gridBorderColor=jQuery("#theme_gridBorderColor").val();
    emsShell.changeTheme(theme)
  }
</script>
[/#macro]

[@displayFrame "!welcome"/]
[@b.foot/]
