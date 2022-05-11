[#macro displayFrame mainHref="" ]
<style>
[#--这一段定制的css，在app模块中的nav.ftl也有一份--]
[#assign sidebar_width=161/]
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
      font-size:13px;
  }
  .nav-legacy.nav-sidebar .nav-item > .nav-link{
    border-radius: 0;
    margin-bottom: 0;
    padding-left:0;
  }
  [#--图标小一点--]
  .nav-sidebar > .nav-item .nav-icon.fa, .nav-sidebar > .nav-item .nav-icon.fab, .nav-sidebar > .nav-item .nav-icon.far, .nav-sidebar > .nav-item .nav-icon.fas, .nav-sidebar > .nav-item .nav-icon.glyphicon, .nav-sidebar > .nav-item .nav-icon.ion{
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
</style>
<div class="wrapper">
    <nav class="main-header navbar navbar-expand navbar-dark navbar-lightblue sticky-top border-bottom-0">
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
                <a href="${b.url('!logout')}" class="btn btn-default btn-flat" target="_top">
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

  <aside class="main-sidebar sidebar-dark-primary elevation-4" style="font-size:13px;">
    <a href="${base}" class="brand-link navbar-lightblue" title="${nav.org.name}" style="height: 47px;border:0px;">
      <img src="${nav.org.logoUrl!}" class="brand-image" style="margin-left: 0rem;"/>
      <span class="brand-text font-weight-light" id="appName" ></span>
    </a>
    <div class="form-inline" style="display:none">
      <div class="input-group">
        <input class="form-control form-control-sidebar" type="search" placeholder="Search" aria-label="Search" id="menu_searcher"
               style="height: 29px;font-size: 12px;">
        <div class="input-group-append">
          <button class="btn btn-sidebar" style="border-left-width: 0px;padding-top: 0px;padding-bottom: 0px;">
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
  [#if mainHref?? && mainHref?length>0 ]
  [@b.div id="main" class="content-wrapper" href="${mainHref}"/]
  [#else]
  [@b.div id="main" class="content-wrapper" /]
  [/#if]

  <aside class="control-sidebar control-sidebar-dark control-sidebar-open" style="display: block;">
    <ul class="nav nav-tabs nav-justified control-sidebar-tabs">
      <li class="nav-item"><a class="nav-link active" href="#control-sidebar-theme-options-tab" data-toggle="tab" aria-expanded="true"><i class="fa fa-wrench"></i></a></li>
      <li class="nav-item"><a class="nav-link" href="#control-sidebar-home-tab" data-toggle="tab" aria-expanded="false"><i class="fa fa-home"></i></a></li>
    </ul>
    <div class="tab-content">
      <div id="control-sidebar-theme-options-tab" class="tab-pane active">
        <h4 class="control-sidebar-heading">布局选项</h4>
        <div class="form-group">
          <label class="control-sidebar-subheading"><input type="checkbox" checked="true" id="sticky_header">固定头部导航</label>
          <label class="control-sidebar-subheading">
            每页数据量<select id="page_size_selector">
              [#list [10,20,30,50,70,100,300] as ps]
              <option value="${ps}" [#if ps==20]selected[/#if]>${ps}</option>
              [/#list]
            </select>
          </label>
        </div>
      </div>
      <div class="tab-pane" id="control-sidebar-home-tab">
        <h4 class="control-sidebar-heading">近期活动</h4>
        <ul class="control-sidebar-menu">
        </ul>
        <h4 class="control-sidebar-heading">工作进程</h4>
        <ul class="control-sidebar-menu">
        </ul>
      </div>
    </div>
  </aside>
  <div class="control-sidebar-bg"></div>
</div>
<script type="text/javascript">
  beangle.load(["adminlte","ems","ems-nav"],function(adminlte,ems,emsnav){
    ems.config.api='${nav.ems.api}';
    var app = {'name':'${nav.app.name}','base':'${nav.app.base}','url':'${nav.app.base}','navStyle':'adminlte'}
    var portal={"name":'platform-portal','url':'${nav.ems.portal}','title':'首页'}
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
      var navMenu = emsnav.createDomainNav(app,portal,${nav.menusJson},params,false);
      navMenu.displayAppMenus('${nav.app.name}');
      [#if nav.profiles??]
      emsnav.createProfileNav();
      [/#if]
      emsnav.setup(params);
      emsnav.enableSearch('menu_searcher');
    });
  });
</script>
[/#macro]
