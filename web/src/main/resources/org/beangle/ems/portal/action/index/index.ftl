[@b.head title="首页"/]
[#macro displayFrame mainHref=""]
<style>
[#--这一段定制的css，在app模块中的nav.ftl也有一份--]
[#assign sidebar_width=156/]
[#--限定宽度为sidebar_widthpx,这两个宽度的css定义要放在一个文件里面,仅仅重置768px,一定要保留991.98px那一段--]
  @media (min-width: 768px) {
   body:not(.sidebar-mini-md):not(.sidebar-mini-xs):not(.layout-top-nav) .content-wrapper,
   body:not(.sidebar-mini-md):not(.sidebar-mini-xs):not(.layout-top-nav) .main-footer,
   body:not(.sidebar-mini-md):not(.sidebar-mini-xs):not(.layout-top-nav) .main-header {
    transition:margin-left .3s ease-in-out;
    margin-left:${sidebar_width}px;
   }
  }
  @media (max-width:991.98px) {
   body:not(.sidebar-mini-md):not(.sidebar-mini-xs):not(.layout-top-nav) .content-wrapper,
   body:not(.sidebar-mini-md):not(.sidebar-mini-xs):not(.layout-top-nav) .main-footer,
   body:not(.sidebar-mini-md):not(.sidebar-mini-xs):not(.layout-top-nav) .main-header {
    margin-left:0
   }
  }

  .sidebar-mini.sidebar-collapse.layout-fixed .main-sidebar:hover .brand-link {
    width:${sidebar_width}px;
  }

  .layout-navbar-fixed .wrapper.sidebar-collapse .main-sidebar:hover .brand-link {
    transition: width 0.3s ease-in-out;
    width: ${sidebar_width}px;
  }

  .main-sidebar, .main-sidebar::before {
    transition: margin-left 0.3s ease-in-out, width 0.3s ease-in-out;
    width: ${sidebar_width}px;
  }

  @media (max-width:767.98px) {
   .main-sidebar, .main-sidebar::before {
    box-shadow:none!important;
    margin-left:-${sidebar_width}px
   }
   .sidebar-open .main-sidebar,
   .sidebar-open .main-sidebar::before {
    margin-left:0
   }
  }
  .layout-fixed .brand-link {
   width:${sidebar_width}px
  }
  .sidebar-mini.sidebar-collapse .main-sidebar:not(.sidebar-no-expand).sidebar-focused,
  .sidebar-mini.sidebar-collapse .main-sidebar:not(.sidebar-no-expand):hover {
    width:${sidebar_width}px
  }
  [#--字体紧凑 靠左--]
  .nav-legacy {
      line-height:1.1;
      width:${sidebar_width}px;
      font-size:0.875rem;
  }
  .nav-legacy.nav-sidebar .nav-item > .nav-link{
    border-radius: 0;
    margin-bottom: 0;
    padding-left:0;
  }
  [#--图标小一点--]
  .nav-sidebar > .nav-item .nav-icon{
    font-size: 0.7rem;
  }
  .nav-sidebar > .nav-item .nav-icon.fa, .nav-sidebar > .nav-item .nav-icon.fas, .nav-sidebar > .nav-item .nav-icon.far, .nav-sidebar > .nav-item .nav-icon.fab, .nav-sidebar > .nav-item .nav-icon.fal, .nav-sidebar > .nav-item .nav-icon.fad, .nav-sidebar > .nav-item .nav-icon.svg-inline--fa, .nav-sidebar > .nav-item .nav-icon.ion {
    font-size: 0.7rem;
  }
  [#--靠左边一点--]
  .text-sm .nav-legacy.nav-sidebar > .nav-item > .nav-link.active > .nav-icon {
    margin-left: 3px;
  }
  [#--层级之间的缩进小一点--]
 .text-sm .nav-legacy.nav-sidebar .nav-item > .nav-link > .nav-icon {
    margin-left: 3px;
  }
  .nav-sidebar .nav-treeview > .nav-item > .nav-link > .nav-icon{
    width: 1.3rem;
  }
  [#--图标窄一点--]
  .nav-sidebar .nav-treeview > .nav-item  .nav-icon {
    width: 1.3rem;
  }
  [#--每个连接的宽度窄一些--]
  .sidebar-mini .main-sidebar .nav-legacy .nav-link, .sidebar-mini-md .main-sidebar .nav-legacy .nav-link, .sidebar-mini-xs .main-sidebar .nav-legacy .nav-link {
    width: ${sidebar_width}px;
  }
  [#--文件夹的箭头靠右一些--]
  .nav-sidebar .nav-link > .right, .nav-sidebar .nav-link > p > .right {
    position: absolute;
    right: 0.1rem;
    top: .7rem;
  }
  [#--缩小时宽度变为3rem--]
  @media (min-width: 992px){
    .sidebar-mini.sidebar-collapse.layout-fixed .brand-link {
      width: 3rem;
    }
    .sidebar-mini.sidebar-collapse .main-sidebar, .sidebar-mini.sidebar-collapse .main-sidebar::before {
      margin-left: 0;
      width: 3rem;
    }
    .sidebar-mini.sidebar-collapse .content-wrapper, .sidebar-mini.sidebar-collapse .main-footer, .sidebar-mini.sidebar-collapse .main-header {
      margin-left: 3rem !important;
    }
  }
  #navbar-setting .nav-link{
    padding:0.3125rem 0.5rem;
  }
  .control-sidebar, .control-sidebar::before {
    width:13rem;
  }
  [#--override bootstrap--]
  .btn-outline-primary {
    color: var(--primary-color);
    border-color: var(--primary-color);
  }
  .btn-outline-primary {
    color: var(--primary-color);
    border-color: var(--primary-color);
  }
  a {
    color: var(--primary-color);
  }
  .btn-primary {
    color: #fff;
    background-color: var(--primary-color);
    border-color: var(--primary-color);
  }
  .sidebar.nav-legacy > .nav-item > .nav-link.active {
    border-color: var(--primary-color);
  }
  .sidebar-dark-lightblue .nav-sidebar.nav-legacy > .nav-item > .nav-link.active, .sidebar-light-lightblue .nav-sidebar.nav-legacy > .nav-item > .nav-link.active {
    border-color: var(--primary-color);
  }
  .card-primary.card-outline {
    border-top: 3px solid var(--primary-color);
  }
  .nav-pills .nav-link.active, .nav-pills .show > .nav-link {
    color: #fff;
    background-color: var(--primary-color);
  }
</style>
<div class="wrapper">
    <nav id="main_header" class="main-header navbar navbar-expand navbar-dark border-bottom-0" style="background-color:var(--navbar-bg-color)">
      <ul class="nav navbar-nav">
         <li class="nav-item">
            <a class="nav-link" data-widget="pushmenu"  title="隐藏/显示菜单" href="#" role="button"><i class="fas fa-bars"></i></a>
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

        <li class="nav-item dropdown notifications-menu">
          <a href="#" class="nav-link" data-toggle="dropdown">
            <i class="far fa-bell"></i>
            <span class="badge badge-warning navbar-badge">0</span>
          </a>
          <ul class="dropdown-menu">
            <li class="nav-header">You have 0 notifications</li>
            <li>
              <ul class="menu">
              </ul>
            </li>
            <li class="footer"><a href="#">View all</a></li>
          </ul>
        </li>

        <li class="nav-item dropdown tasks-menu">
          <a href="#" class="nav-link" data-toggle="dropdown">
            <i class="far fa-flag"></i>
            <span class="badge badge-warning navbar-badge">0</span>
          </a>
          <ul class="dropdown-menu">
            <li class="nav-header">You have 0 tasks</li>
            <li>
              <ul class="menu">
              </ul>
            </li>
            <li class="footer">
              <a href="#">View all tasks</a>
            </li>
          </ul>
        </li>
        <li class="nav-item">
          <a class="nav-link" data-widget="fullscreen" href="#" role="button">
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
                ${nav.principal.description} - (${nav.principal.name})[#if nav.username != nav.principal.name] 模拟${nav.username}[/#if]
                <small>[#if nav.principal.remoteToken??]统一身份平台登录[#else]本地登录[/#if]</small>
              </p>
            </li>
            <li class="user-footer">
              [#if !nav.principal.credentialReadOnly]
              <div class="float-sm-left">
                <a href="/cas/edit" class="btn btn-default btn-flat"><i class="nav-icon far fa-user"></i>修改密码</a>
              </div>
              [/#if]
              <div class="float-sm-right">
                <a href="${b.url('!logout')}" onclick="emsnav.clearNavState();return true;" class="btn btn-default btn-flat" target="_top">
                  <i class="nav-icon fa fa-door-open"></i>退出&nbsp;&nbsp;
                </a>
              </div>
            </li>
          </ul>
        </li>
        <li class="nav-item">
          <a href="#" style="padding:0.3125rem 0.35rem" class="nav-link" data-slide="true" data-widget="control-sidebar"><i class="fa fa-cog"></i></a>
        </li>
      </ul>
    </nav>

  <aside id="main_siderbar" class="main-sidebar sidebar-light-lightblue elevation-4" style="font-size:0.875rem;overflow: hidden;">
    <a href="${b.base}" class="brand-link title="${nav.org.name} ${nav.domain.title}" style="border:0px;background-color:var(--navbar-bg-color)" onclick="emsnav.clearNavState();return true;">
      <img src="${nav.domain.logoUrl!}" class="brand-image" style="margin-left: 0rem;"/>
      <span class="brand-text font-weight-light" id="appName" style="font-size: 1rem;color: rgba(255,255,255,.8);"></span>
    </a>
    <div class="form-inline" style="display:none">
      <div class="input-group">
        <input class="form-control form-control-sidebar" type="search" placeholder="Search" aria-label="Search" id="menu_searcher"
               style="height: 29px;font-size: 12px;border:0px;border-radius: 0px;">
        <div class="input-group-append">
          <button class="btn btn-sidebar" style="border:0px;padding-top: 0px;padding-bottom: 0px;border-radius: 0px;">
            <i class="fas fa-search fa-fw" style="width: 0.6rem;"></i>
          </button>
        </div>
      </div>
      <div class="sidebar-search-results"><div class="list-group"></div></div>
    </div>
    <div class="sidebar" style="padding-right:0px">
      <nav class="mt-2">
        <ul id="menu_ul" class="nav nav-pills nav-sidebar flex-column nav-legacy nav-child-indent" data-widget="treeview" role="menu" data-accordion="false"></ul>
      </nav>
    </div>
  </aside>
  <div class="content-wrapper" id="main_wrapper">
    [@b.div id="main"/]
  </div>

  <aside id="control_sidebar" class="control-sidebar control-sidebar-light control-sidebar-open" style="display: block;">
    <ul class="nav nav-tabs nav-justified control-sidebar-tabs">
      <li class="nav-item"><a class="nav-link active" style="padding: .4rem .8rem;" href="#control-sidebar-theme-options-tab" data-toggle="tab" aria-expanded="true"><i class="fa fa-wrench"></i></a></li>
      <li class="nav-item"><a class="nav-link" style="padding: .4rem .8rem;" href="#control-sidebar-home-tab" data-toggle="tab" aria-expanded="false"><i class="fa fa-home"></i></a></li>
    </ul>
    <div class="tab-content">
      <div id="control-sidebar-theme-options-tab" class="tab-pane active" style="padding: 10px 15px;">
        <h6 class="control-sidebar-heading">布局选项</h6>
        <div class="form-group">
          <div class="mb-2"><input type="checkbox" id="sticky_header"><label for="sticky_header">固定头部导航</label></div>
          <div class="mb-2">
            导航风格:
            <input name="nav_siderbar_theme" value="light" checked="checked" id="nav_siderbar_theme_light" type="radio" onclick="emsnav.changeNavSidebarTheme(this.value)">
              <label for="nav_siderbar_theme_light">浅白</label>
            <input name="nav_siderbar_theme" value="dark" id="nav_siderbar_theme_dark" type="radio" onclick="emsnav.changeNavSidebarTheme(this.value)">
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
              [#list [10,20,30,50,70,100,300] as ps]
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
              <li><button class="btn btn-outline-primary btn-sm" onclick="emsnav.changeTheme(null,true)">恢复默认值</button>
            </ul>
          </div>
        </div>
      </div>
      <div class="tab-pane" id="control-sidebar-home-tab" style="padding: 10px 15px;">
        <h6 class="control-sidebar-heading">近期活动</h6>
        <ul class="control-sidebar-menu" style="padding-left: 20px;">
          <li>时间 <div id="clock" style="display:inline"></div></li>
        </ul>
      </div>
    </div>
  </aside>
  <div class="control-sidebar-bg"></div>
</div>
<script type="text/javascript">
  beangle.load(["adminlte","ems","ems-nav"],function(adminlte,ems,emsnav){
    ems.config.api='${nav.ems.api}';
    var app = {'name':'${nav.app.name}',"title":'${nav.domain.title}','base':'${nav.app.base}','url':'${b.url('!index')}','navStyle':'adminlte'}
    var params={}
    [#list nav.params as k,v]
    params['${k}']='${v}';
    [/#list]
    [#if nav.profiles??]
    ems.init(${nav.profiles},${nav.cookie!'null'});
    if(ems.config.profiles.length>0 && ems.config.profile){
      var default_p = ems.config.profile
      for(var i in default_p){
        if(i != "id") params[i] = default_p[i];
      }
      params['maxTopItem']=8;
    }
    [/#if]

    jQuery(document).ready(function(){
      emsnav.createNav(app,app,${nav.menusJson},params,true);
      [#if nav.profiles??]
      emsnav.createProfileNav();
      [/#if]
      [#if mainHref?? && mainHref?length>0 ]
      emsnav.setWelcomeUrl('${b.url(mainHref)}');
      [/#if]
      var theme={"primaryColor": "${nav.theme.primaryColor}","navbarBgColor": "${nav.theme.navbarBgColor}", "searchBgColor": "${nav.theme.searchBgColor}", "gridbarBgColor": "${nav.theme.gridbarBgColor}", "gridBorderColor": "${nav.theme.gridBorderColor}"}
      emsnav.setup(theme,params);
      setTimeout(function(){jQuery("#main_siderbar .brand-link").css("height",jQuery("#main_header").outerHeight()+"px");}, 1500);
      emsnav.enableSearch('menu_searcher');
      window.emsnav=emsnav;
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
    emsnav.changeTheme(theme)
  }
</script>
[/#macro]

[@displayFrame "!welcome"/]
[@b.foot/]
