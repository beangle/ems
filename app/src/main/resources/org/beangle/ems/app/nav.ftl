[#macro displayFrame mainHref="" ]
<style>
[#--限定宽度为171px--]
@media (min-width: 768px) {
  body:not(.sidebar-mini-md) .content-wrapper,
  body:not(.sidebar-mini-md) .main-footer,
  body:not(.sidebar-mini-md) .main-header {
    transition: margin-left 0.3s ease-in-out;
    margin-left: 171px;
  }
}
.main-sidebar, .main-sidebar::before {
  transition: margin-left 0.3s ease-in-out, width 0.3s ease-in-out;
  width: 171px;
}
@media (max-width: 767.98px) {
  .main-sidebar, .main-sidebar::before {
    box-shadow: none !important;
    margin-left: -171px;
  }
  .sidebar-open .main-sidebar, .sidebar-open .main-sidebar::before {
    margin-left: 0;
  }
}
.layout-fixed .brand-link {
    width: 171px;
}
.layout-navbar-fixed .wrapper.sidebar-collapse .main-sidebar:hover .brand-link {
  transition: width 0.3s ease-in-out;
  width: 171px;
}

.sidebar-mini.sidebar-collapse .main-sidebar:hover, .sidebar-mini.sidebar-collapse .main-sidebar.sidebar-focused {
  width: 171px;
}
[#--字体紧凑 靠左--]
.nav-legacy {
    line-height:1.4;
    background-color:rgb(34, 45, 50);
    width:171px;
}
.nav-legacy.nav-sidebar .nav-item > .nav-link{
  border-radius: 0;
  margin-bottom: 0;
  padding-left:0;
}
[#--图标小一点--]
.nav-sidebar > .nav-item .nav-icon.fa, .nav-sidebar > .nav-item .nav-icon.fab, .nav-sidebar > .nav-item .nav-icon.far, .nav-sidebar > .nav-item .nav-icon.fas, .nav-sidebar > .nav-item .nav-icon.glyphicon, .nav-sidebar > .nav-item .nav-icon.ion{
  font-size: 0.9rem;
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

      <ul class="navbar-nav ml-auto">
        <li class="nav-item dropdown">
          <a href="#" class="nav-link" data-toggle="dropdown">
            <i class="far fa-comments"></i>
            <span class="badge badge-danger navbar-badge" id="newly-message-count">0</span>
          </a>
          <div id="newly-message" class="dropdown-menu dropdown-menu-lg dropdown-menu-right" style="left: inherit; right: 0px;min-width:280px">
          </div>
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
        <li class="nav-item dropdown user user-menu">
          <a href="#" class="nav-link" data-toggle="dropdown" title="${nav.principal.description}" style="padding:.35rem 0.35rem">
            <img src="${nav.avatarUrl}" class="user-image">
          </a>
          <ul class="dropdown-menu">
            <li class="user-header">
              <img src="${nav.avatarUrl}" class="img-circle" alt="User Image">
              <p>
                ${nav.principal.description} - (${nav.principal.name})[#if nav.username != nav.principal.name] 模拟${nav.username}[/#if]
                <small>[#if nav.principal.remoteToken??]本地登录[#else]统一身份平台登录[/#if]</small>
              </p>
            </li>
            <li class="user-footer">
              <div class="float-sm-left">
                <a href="/cas/edit" class="btn btn-default btn-flat"><i class="nav-icon far fa-user"></i>修改密码</a>
              </div>
              <div class="float-sm-right">
                <a href="${b.url('!logout')}" class="btn btn-default btn-flat" target="_top">
                  <i class="nav-icon fa fa-door-open"></i>退出&nbsp;&nbsp;
                </a>
              </div>
            </li>
          </ul>
        </li>
        <li class="nav-item">
          <a href="#" style="padding:.35rem 0.35rem" class="nav-link" data-slide="true" data-widget="control-sidebar"><i class="fa fa-cog"></i></a>
        </li>
      </ul>
    </nav>

  <aside class="main-sidebar sidebar-dark-primary elevation-4">
    <a href="${base}" class="brand-link navbar-lightblue" title="${nav.org.name}">
      <img src="${nav.org.logoUrl!}" class="brand-image"/>
      <span class="brand-text font-weight-light" id="appName" ></span>
    </a>
    <div class="sidebar" style="background-color:#222d32">
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
    var app = {'name':'${nav.app.name}','base':'${nav.app.base}','url':'${nav.app.base}','navStyle':'beangle-webui-bootstrap'}
    var portal={"name":'ems-portal','url':'${nav.ems.portal}','title':'首页'}
    var params={}
    [#list nav.params as k,v]
    params['${k}']='${v}';
    [/#list]
    [#if nav.profiles??]
    ems.init(${nav.profiles},${nav.cookie!'null'});
    if(ems.config.profiles.length>0 && ems.config.profile){
      var default_p = ems.config.profile
      for(var i in default_p){
        if(i != "id"){
          params[i] = default_p[i];
        }
      }
      params['maxTopItem']=7;
    }
    [/#if]
    jQuery(document).ready(function(){
      emsnav.createGroupNav(app,portal,${nav.menusJson},params);
      [#if nav.profiles??]
      emsnav.createProfileNav();
      [/#if]
      emsnav.setup(params);
    });
  });
</script>
[/#macro]
