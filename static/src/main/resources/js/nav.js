// ems nav
  function messageCallBack(c){
    jQuery('#newly-message-count').text(c)
  }

  if (typeof String.prototype.endsWith != 'function') {
    String.prototype.endsWith = function(suffix) {
       return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
  }

(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('ems')) :
  typeof define === 'function' && define.amd ? define(['exports', 'ems'], factory) :
  (global = global || self, factory(global.emsnav = {}, global.ems));
}(this, (function (exports, ems) { 'use strict';
 /**
  * @domainMenus
  *    {domain:{},groups:[{group:{},appMenus:[{app:{},menus:[]},{app:{},menus:[]}] }] }
  */
  function Nav(app,portal,domainMenus,params){
    this.portal=portal;
    this.app=app;
    this.apps=[];
    this.groups=[];
    this.appMenus={};
    this.menuDomId="menu_ul";
    this.navDomId="top_nav_bar";
    this.sysName=null;
    this.params={};
    this.maxTopItem=9;
    this.welcomeUrl=null;
    if(params){
      for(var name in params){
        var pv=params[name];
        if("menuDomId" == name){
          this.menuDomId=pv;
        }else if ("navDomId" == name){
          this.navDomId=pv;
        }else if ("sysName" == name){
          this.sysName=pv;
        }else if ("maxTopItem" == name){
          this.maxTopItem=Number.parseInt(pv);
        }else{
          this.params[name]=pv;
        }
      }
    }
    this.currentGroupId="";
    this.groupMenus=domainMenus.groups;
    for(var i=0;i < this.groupMenus.length; i++){
      this.groups.push(this.groupMenus[i].group);
    }

    this.getIconClass=function(name){
      if(name.indexOf("设置") > -1){
        return "fas fa-cog";
      }else if(name.endsWith("开关")){
       return "fa fa-toggle-on";
      }else if(name.endsWith("信息")){
        return "fa fa-info-circle";
      }else if(name.indexOf("查询") > -1){
        return "fa fa-search";
      }else if(name.indexOf("打印") > -1){
        return "fa fa-print";
      }else if(name.indexOf("统计") > -1){
        return "fa fa-chart-bar";
      }else if(name.indexOf("安排") > -1){
        return "fa fa-calendar";
      }else if(name.indexOf("排名") > -1){
        return "fa fa-sort-amount-down";
      }else if(name.endsWith("表")){
        return "fa fa-table";
      }else{
        return "far fa-circle";
      }
    }
    this.menuTempalte='<li class="nav-item"><a class="nav-link" href="{menu.entry}" target="{menu.target}" onclick="return emsnav.openMenu(this,\'{menu.target}\',{menu.iframe})"><i class="nav-icon {icon_class}"></i><p>{menu.title}</p></a></li>';
    this.foldTemplate='<li class="nav-item has-treeview {open_class}"><a class="nav-link {active_class}" href="javascript:void(0)"><i class="nav-icon {icon_class}"></i><p>{menu.title}<i class="nav-icon fa fa-angle-left right"></i></p></a><ul class="nav nav-treeview" id="menu{menu.id}"></ul></li>'
    this.appFoldTemplate='<li class="nav-item has_treeview {open_class}"><a class="nav-link {active_class}" href="javascript:void(0)"><i class="nav-icon {icon_class}"></i><p>{app.title}<i class="nav-icon fa fa-angle-left right"></i></p></a><ul class="nav nav-treeview" id="menu_app{app.id}"></ul></li>'

    this.groupTemplate='<li class="nav-item"><a class="nav-link {active_class}" href="javascript:void(0)" id="group_{group.id}">{group.title}</a></li>';
    this.portalTemplate='<li class="nav-item"><a href="{app.url}" class="nav-link" target="_self">{app.title}</a></li>';
    this.dropdownGroupNavTemplate='<a href="javascript:void(0)"  class="dropdown-item {active_class}" id="group_{group.id}">{group.title}</a>'
    if(!this.app.navStyle){
     this.app.navStyle="unknown";
    }
    this.collectApps();
    this.processMenus();
    if(!this.sysName){
      this.sysName=this.app.title;
    }
    jQuery("#"+this.menuDomId).addClass("sidebar-menu");
  }

  Nav.prototype={
    setWelcomeUrl:function(url){
      this.welcomeUrl=url;
    },
    processUrl : function(url){
      if(url.indexOf('{') == -1) return url;
      for(var name in this.params){
        url = url.replace('{'+name+'}',this.params[name]);
      }
      return url;
    },
    /**
     * 收集domain中的apps，以及每个app对应的菜单
     */
    collectApps : function(){
      for(var p=0;p < this.groupMenus.length;p++){
        var childrenApps=this.groupMenus[p].appMenus; // a group contain many app
        var group=this.groupMenus[p].group;
        for(var i=0;i<childrenApps.length;i++){
          var app = childrenApps[i].app;
          if(!app.group){
            app.group=group;
          }
          this.appMenus[app.name]=childrenApps[i].menus;
          if(app.name==this.portal.name){
            if(!this.portal.title){
              this.portal.title=app.title;
            }
            this.portal.group=group;
            this.portal.url=app.url;
          } else if(app.name==this.app.name){
            app.base=this.app.base;
            this.app.group=group;
            this.app.title=app.title;
            this.app.id=app.id;
            this.apps.push(app);
          } else {
            this.apps.push(app);
          }
          //去除appbase的结尾斜线，因为资源的形式为/path/to/uri
          if(app.base.endsWith("/")){
            app.base=app.base.substring(0,app.base.length-1);
          }
          app.base = this.processUrl(app.base);
          app.iframe=(app.navStyle != this.app.navStyle);
          app.embeddable=ems.sameDomain(this.app.base,app.base);
        }
      }
    },

    searchMenu : function(name,limit){
      if(!limit) limit =10;
      var results = [];
      for(var p=0;p < this.groupMenus.length;p++){
        var childrenAppMenus=this.groupMenus[p].appMenus;
        for(var i=0;i<childrenAppMenus.length;i++){
          this.searchMenuByName(childrenAppMenus[i].menus,name,limit,results,[]);
          if(results.length >= limit) return results;
        }
      }
      return results;
    },

    searchMenuByName : function(menus,name,limit,results,path){
      for(var j=0;j < menus.length;j++){
        if(results.length >= limit) return;
        var menu=menus[j];
        if(menu.entry && (menu.title.includes(name) || menu.entry.toLowerCase().includes(name))){
          var resultPath=path.slice();
          resultPath.push(menu.title);
          var result = {'name':menu.title,'link':menu.entry,'path':resultPath,'target':menu.target,'iframe':menu.iframe}
          results.push(result);
          if(results.length >= limit) break;
        }else{
          if(menu.children && menu.children.length>0) {
            path.push(menu.title);
            this.searchMenuByName(menu.children,name,limit,results,path);
          }
        }
      }
      if(path.length>0) path.pop();
    },
    locateMenu : function(url){
      for(var p=0;p < this.groupMenus.length;p++){
        var appMenus=this.groupMenus[p].appMenus;
        for(var i=0;i<appMenus.length;i++){
          var m = this.locateMenuByHref(appMenus[i].menus,url);
          if(m) return {"group":this.groupMenus[p].group,"app":appMenus[i],"menu":m};
        }
      }
      return null;
    },
    locateMenuByHref : function(menus,url){
      var menu=null;
      for(var j=0;j < menus.length;j++){
        menu=menus[j];
        if(menu.entry && url.startsWith(menu.entry)){
          return menu;
        }else{
          if(menu.children && menu.children.length>0) {
             menu = this.locateMenuByHref(menu.children,url);
             if(menu) return menu;
          }
        }
      }
      return null;
    },
    processMenus : function(){ // 修改和构架menu的入口和target
      for(var p=0;p < this.groupMenus.length;p++){
        var childrenAppMenus=this.groupMenus[p].appMenus;
        for(var i=0;i<childrenAppMenus.length;i++){
          var app = childrenAppMenus[i].app;
          this.processMenuEntry(app,childrenAppMenus[i].menus);
        }
      }
    },

    processMenuEntry : function(app,menus){
      for(var i=0; i <menus.length;i++){
        var menu = menus[i];
        if(menu.entry && !menu.entry.startsWith("http")){
           if(app.embeddable) menu.target="main"; else menu.target="_blank";
           menu.iframe = app.iframe;
           menu.entry = this.processUrl(app.base + menu.entry);
        }
        if(menu.children) this.processMenuEntry(app,menu.children);
      }
    },
    /**
     * 在左侧菜单栏创建菜单
     * @param openMenuId may be appId,menuId
     */
    createMenus : function(jqueryElem,menus,openMenuId){
      jqueryElem.empty();
      if(!openMenuId) openMenuId = -1 ;
      var menuItem='';
      for(var i=0; i <menus.length;i++){
        var menu = menus[i];
        var fonticon="fa fa-list"
        if(menu.menus){//menu is an app
          var appItem = this.appFoldTemplate.replace('{app.id}',menu.app.id);
          appItem = appItem.replace('{icon_class}',fonticon);
          appItem = appItem.replace('{app.title}',menu.app.title);
          appItem = appItem.replace('{open_class}',(openMenuId==menu.app.id)?"menu-open":"");
          appItem = appItem.replace('{active_class}',(openMenuId==menu.app.id)?"active":"");
          jqueryElem.append(appItem);
          this.createMenus(jQuery('#menu_app'+menu.app.id),menu.menus);
        }else if(menu.children){//fold
          menuItem = this.foldTemplate.replace('{menu.id}',menu.id);
          if(menu.fonticon) fonticon = menu.fonticon;
          menuItem = menuItem.replace('{icon_class}',fonticon);
          menuItem = menuItem.replace('{menu.title}',menu.title);
          menuItem = menuItem.replace('{open_class}',(openMenuId==menu.id)?"menu-open":"");
          menuItem = menuItem.replace('{active_class}',(openMenuId==menu.id)?"active":"");
          jqueryElem.append(menuItem);
          this.createMenus(jQuery('#menu'+menu.id),menu.children);
        }else{//menu
          menuItem = this.menuTempalte.replace('{menu.id}',menu.id);
          menuItem = menuItem.replace('{menu.title}',menu.title);
          if(menu.fonticon){ fonticon = menu.fonticon; }
          else{ fonticon = this.getIconClass(menu.title);}
          menuItem = menuItem.replace('{icon_class}',fonticon);
          menuItem = menuItem.replace('{menu.entry}',menu.entry);
          menuItem = menuItem.replace('{menu.target}',menu.target);
          menuItem = menuItem.replace('{menu.target}',menu.target);//without replaceAll
          menuItem = menuItem.replace('{menu.iframe}',menu.iframe);
          jqueryElem.append(menuItem);
        }
      }
    },
    activate : function(){
      var that=this;
      jQuery("#"+this.menuDomId+" li a").click(function() {
        if(this.href=="javascript:void(0)"){
          var jThis=jQuery(this);
          jThis.parent('li').siblings().each(function (i,li){
              jQuery(li).removeClass('menu-open menu-is-opening');
              jQuery(li).children('ul').hide();
              jQuery(li).children('a').removeClass('active');
            }
          );
          if(jThis.hasClass("active")){
            jThis.removeClass("active");
          }else{
            jThis.addClass("active");
          }
        }else{
          jQuery(this).parent('li').siblings().each(function (i,li){jQuery(li).children('a').removeClass('active')});
          jQuery(this).addClass('active');
        }
      });
      var navMenuDomId =this.menuDomId;
      setTimeout(function(){
        //FIXME treeview sometimes missing domcument onloading events
        if(!jQuery("#"+navMenuDomId).data("lte.treeview")){
           jQuery.fn.Treeview.call(jQuery("#"+navMenuDomId),"init");
        }
      },1000);
    },
    openMenu : function(obj,target,iframe){
      if(target=="_blank") return true;
      var targetEle = document.getElementById(target)
      if(typeof obj =="object" && obj.tagName.toLowerCase()=="a"){
        this.currentMenuHref = obj.href;
      }else if (typeof obj =="string"){
        this.currentMenuHref = obj;
      }

      var mainWrapper = targetEle.parentNode;
      if(iframe){
        if(targetEle.tagName=='DIV'){
          mainWrapper.innerHTML="";
          var f = document.createElement('iframe')
          f.setAttribute("width","100%");
          f.setAttribute("height","100%");
          f.setAttribute("SCROLLING","auto");
          f.setAttribute("FRAMEBORDER","0");
          f.style.minHeight=(document.getElementById("main_siderbar").offsetHeight+"px");
          f.setAttribute("name",target);
          f.id=target;
          mainWrapper.appendChild(f);
        }
        return true;
      }else{
        if(targetEle.tagName=='DIV'){
          beangle.Go(obj,target);
        }else if(targetEle.tagName=='IFRAME'){
          mainWrapper.removeChild(targetEle);
          var f = document.createElement('div')
          f.setAttribute("width","100%");
          f.setAttribute("height","100%");
          f.setAttribute("class","ajax_container");
          f.id=target;
          mainWrapper.appendChild(f);
          beangle.Go(obj,target);
        }
        return false;
      }
    },
    fillAppName : function(){
      if(this.sysName.length>6){
        jQuery('#appName').css("font-size","0.875rem")
      }
      jQuery('#appName').html(this.sysName);
    },
    /**
     * 添加顶层groups
     */
    addTopGroups:function(jqueryElem){
      var appItem='';
      var topItemCount=0;
      var topMoreHappened=false;
      this.fillAppName();
      prependApps(jqueryElem,this,this.apps,true)

      for(var i=0;i < this.groups.length; i++){
        var group = this.groups[i];
        topItemCount += 1;
        if(topItemCount == this.maxTopItem && this.groups.length > this.maxTopItem){
          jqueryElem.append('<li class="nav-item dropdown"><a href="#" data-toggle="dropdown" class="dropdown-toggle nav-link">更多...</a><div id="topMore" aria-labelledby="navbarDropdown" class="dropdown-menu"></div><li>');
          topMoreHappened=true;
        }
        if(topMoreHappened){
          jqueryElem = jQuery('#topMore');
        }
        if(topMoreHappened){
          appItem = this.dropdownGroupNavTemplate.replace('{group.id}',group.id);
        }else{
          appItem = this.groupTemplate.replace('{group.id}',group.id);
        }
        appItem = appItem.replace('{group.title}',group.title);
        appItem = appItem.replace('{group.name}',group.name);
        appItem = appItem.replace('{active_class}',(i==0)?"active":"");
        jqueryElem.append(appItem);
        jQuery("#group_"+group.id).click(function(){changeGroup(this);return false});
      }
    },

    displayCurrent:function(){
      this.displayGroupMenus(this.currentGroupId);
    },

    displayAppMenus:function(appName){
      var groupId=0,appId=0;
      if (appName == this.portal.name){
         groupId = this.portal.group.id;
      }else{
        var apps = this.apps;
        for( var i=0; i < apps.length; i++){
           if(apps[i].name==appName){
             appId=apps[i].id;
             groupId=apps[i].group.id;
             break;
           }
        }
      }
      if(!groupId){
        console.log("error app-name "+ appName);
        groupId = this.groupMenus[0].group.id
      }
      this.displayGroupMenus(groupId,appId)
    },
    /**显示指定group的menu*/
    displayGroupMenus : function (groupId,appId,menuObj){
      this.currentGroupId = groupId;
      switchNavActive("#group_"+groupId);
      for(var i=0;i < this.groupMenus.length; i++){
        var groupMenu=this.groupMenus[i];
        if(groupMenu.group.id==groupId){
          document.title=groupMenu.group.title;
          var openMenuId = appId;
          if(groupMenu.appMenus.length > 0 && !openMenuId){
            openMenuId = groupMenu.appMenus[0].app.id;
          }
          //当仅有一个app的时候，就忽略展现app的名字
          if(groupMenu.appMenus.length ==1){
            var onlyOneAppMenu=groupMenu.appMenus[0]
            if(onlyOneAppMenu.menus.length>0) openMenuId=onlyOneAppMenu.menus[0].id;
            this.createMenus(jQuery('#'+this.menuDomId),onlyOneAppMenu.menus,openMenuId);
          }else{
            this.createMenus(jQuery('#'+this.menuDomId),groupMenu.appMenus,openMenuId);
          }
          this.currentGroupId=groupId;
          this.activate();
          break;
        }
      }
      if(menuObj && menuObj.entry){
        var menu = jQuery('#main_siderbar a[href="'+menuObj.entry+'"]')
        menu.parents('li').each(function (i,li){
          jQuery(li).addClass('menu-open');
          jQuery(li).siblings().each(function (i,sli){
              jQuery(sli).removeClass('menu-open menu-is-opening');
              jQuery(sli).children('ul').hide();
            }
          );
        });
        menu.trigger("click");
     }
    },
    /** 菜单查找*/
    search:function(name,limit){
      var results = this.searchMenu(name,limit);
      var searchRegExp = new RegExp(name, 'gi');
      var resultDom = jQuery(".sidebar-search-results .list-group");
      resultDom.empty();
      if(results.length ==0){
        var notfound={'name':"找不到结果",'link':'#','path':[],'target':"main",'iframe':false}
        resultDom.append(this.renderSearchItem(searchRegExp,notfound))
      }else{
        for(var i=0;i < results.length;i++){
          var result = results[i];
          resultDom.append(this.renderSearchItem(searchRegExp,result));
        }
      }
      this.openSearchResults();
    },

    openSearchResults : function(){
      var searchDom =jQuery('#'+this.searchInputId).parent().parent();
      searchDom.addClass("sidebar-search-open");
      searchDom.find(".input-group-append .btn i").removeClass("fa-search").addClass("fa-times");
    },

    closeSearchResults : function(){
      var searchDom =jQuery('#'+this.searchInputId).parent().parent();
      searchDom.removeClass("sidebar-search-open");
      searchDom.find(".input-group-append .btn i").removeClass("fa-times").addClass("fa-search");
    },

    toggleSearchResults : function(){
      var searchDom =jQuery('#'+this.searchInputId).parent().parent();
      if (searchDom.hasClass("sidebar-search-open")) {
        this.closeSearchResults();
      } else {
        this.openSearchResults();
      }
    },

    renderSearchItem : function(searchRegExp,result){
      var pathStr = result.path.join(" -> ");
      var name = result.name.replace(searchRegExp, function (str) {
        return "<strong class=\"text-light\">" + str + "</strong>";
      });
      var item = null;
      if(result.link != '#'){
        var onclickFunc="return emsnav.openMenu(this,\'"+result.target+"\',"+result.iframe+")";
        item = jQuery('<a/>', {href: decodeURIComponent(result.link), class: 'list-group-item', target: result.target,onclick: onclickFunc });
      }else{
        item = jQuery('<a/>', {href: decodeURIComponent(result.link), class: 'list-group-item', target: result.target });
      }
      var searchTitleElement = jQuery('<div/>', {class: 'search-title' }).html(name);
      var searchPathElement = jQuery('<div/>', {class: 'search-path'}).html(pathStr);
      item.append(searchTitleElement).append(searchPathElement);
      return item;
    }
  }

  /**
   * 切换顶部导航栏上的按钮
   */
  function switchNavActive(anchorId){
    if(jQuery(anchorId).parent()[0].tagName=="LI"){
      jQuery(anchorId).parent().siblings().each(function(i,li){jQuery(li).children("a").removeClass("active")});
      jQuery(anchorId).addClass("active");
    }else{
      jQuery(anchorId).siblings().each(function(i,a){jQuery(a).removeClass("active")});
      jQuery(anchorId).addClass("active");
    }
  }

  /**
   * 添加app导航,是app内部菜单之外的所有app的展示panel
   */
  function prependApps(jqueryElem,nav,apps,autohide){
    var appDropNav='<ul class="nav navbar-nav"><li class="nav-item dropdown">' +
                   '<a href="#" data-toggle="dropdown" class="nav-link {autohide}" role="button" title="应用" class="dropdown-toggle" aria-haspopup="true" aria-expanded="true"><i class="fas fa-th"></i></a>' +
                   '<div id="app_drop_bar" class="dropdown-menu columns-3"></div>'+
                   '</li></ul>';
    var appTemplate='<a href="{app.url}" class="dropdown-item {active_class}" target="_top">{app.title}</a>';
    jqueryElem.before(appDropNav.replace("{autohide}",autohide?"app-toggle":""));
    var appDropBarID="#app_drop_bar";
    jqueryElem = jQuery(appDropBarID);
    var curGroupId=0;
    if(!apps){
      apps= nav.apps;
    }
    var columRows=Math.ceil(apps.length/3);
    var content='<div class="row">';
    var columnApps=[[],[],[]]
    for(var i=0;i<apps.length;i++){
      columnApps[Math.floor(i / columRows)].push(apps[i]);
    }
    for(var column=0;column<columnApps.length;column++){
      var columnApp= columnApps[column];
      var columnDiv='<div class="col-sm-4">'
      for(var i=0;i<columnApp.length;i++){
        var app=columnApp[i];
        if(app.group){
          if(curGroupId ==0){
            curGroupId=app.group.id;
          }else{
            if(app.group.id != curGroupId){
              if(i>0){
                columnDiv+='<div class="dropdown-divider"></div>';
              }
              curGroupId=app.group.id;
            }
          }
        }
        if(app.name==nav.app.name){//添加左侧的标题
          columnDiv += '<a  class="dropdown-item active" href="#">'+app.title+'</a>';
        }else{
          var appendHtml = appTemplate.replace('{app.url}',nav.processUrl(app.url));
          appendHtml = appendHtml.replace('{app.title}',app.title);
          appendHtml = appendHtml.replace('{active_class}',"");
          columnDiv+=appendHtml;
        }
      }
      columnDiv+="</div>";
      content += columnDiv;
    }
    content+="</div>";
    jqueryElem.append(content);
  }
  var nav ={};

  function createNav(app,portal,domainMenus,params,displayFirstGroup){
    nav = new Nav(app,portal,domainMenus,params);
    nav.addTopGroups(jQuery('#'+nav.navDomId));
    if(displayFirstGroup && nav.groups.length>0){
      nav.displayGroupMenus(nav.groups[0].id);
    }
    return nav;
  }

  /**
   * 切换group的全局函数
   * @param id
   * @param name
   * @returns
   */
  function changeGroup(ele){
    var id=ele.id
    nav.displayGroupMenus(id.substring("group_".length));
  }

  function createProfileNav(){
    var profileSelectTemplate=
     '<li class="nav-item dropdown">' +
        '<a class="dropdown-toggle nav-link" data-toggle="dropdown" href="#" id="profile_switcher" aria-expanded="false">{first}</a> '+
        '<div class="dropdown-menu">{list}</div>' +
    '</li>';
    var profileTemplate='<a href="{profile.url}" class="dropdown-item">{profile.name}</a>'
    var profiles=ems.config.profiles;
    if(profiles.length > 1){ //display profile when multi profile occur
      var profile = ems.config.profile;
      var profilehtml= profileSelectTemplate.replace('{first}',profile.name);
      var list="";
      for(var i=0;i<profiles.length;i++){
        if(profiles[i].id != profile.id){
          var profileItem=profileTemplate.replace("{profile.url}",profiles[i].url);
          profileItem=profileItem.replace("{profile.name}",profiles[i].name);
          list +=profileItem
        }
      }
      profilehtml = profilehtml.replace('{list}',list);
      jQuery('.main-header > .ml-auto').prepend(profilehtml)
    }
  }

  function fetchMessages(params){
    if(!ems.sameDomain(window.location.href,params['webapp'])){
      return;
    }
    jQuery.ajax({
      url: params['webapp']+'/portal/user/message/newly?callback=messageCallBack',cache:false,
      type: "GET",dataType: "html",
      complete: function( jqXHR) {
        try{
          jQuery("#newly-message").html(jqXHR.responseText);
        }catch(e){alert(e)}
      }
    });
  }

  function setLocal(name,value){
    if(localStorage){
      if(value){
        localStorage.setItem(name,value);
      }else{
        localStorage.removeItem(name);
      }
    }
  }
  function getLocal(name,defaultValue){
    if(localStorage){
      return (localStorage.getItem(name) || defaultValue);
    }else{
      return defaultValue;
    }
  }

  function setup(theme,params) {
    nav.theme = theme;
    jQuery("body").addClass("sidebar-mini layout-fixed text-sm");
    document.documentElement.style.setProperty("scrollbar-width","thin");
    fetchMessages(params);
    var stickyHeader = getLocal("beangle.ems.nav_sticky_header","1")
    if(stickyHeader=="1") {
      jQuery('#main_header').addClass("sticky-top")
      jQuery("#sticky_header").prop("checked", true);
    }

    jQuery("#control_sidebar input[name=root_font_size]").on("click",function(e){changeFontSize(jQuery(e.target).val())});
    jQuery("#sticky_header").on("click",function(event){
      if(this.checked){
        jQuery('#main_header').addClass("sticky-top");
        if(localStorage)localStorage.setItem("beangle.ems.nav_sticky_header","1");
      }else{
        jQuery('#main_header').removeClass("sticky-top");
        if(localStorage)localStorage.setItem("beangle.ems.nav_sticky_header","0");
      }
    });

    changeNavSidebarTheme(getLocal("beangle.ems.nav_sidebar_theme","--"));
    changeFontSize(getLocal("beangle.ems.root_font_size","--"));
    applyTheme(getLocal("beangle.ems.theme",theme))

    var pageSize = beangle.cookie.get("pageSize");
    if(pageSize) jQuery("#page_size_selector").val(pageSize);
    jQuery("#page_size_selector").on("change",function(event){
      beangle.cookie.set("pageSize",this.value,"/",10*365);
    });
    jQuery("#main_siderbar .brand-link").css("height",jQuery("#main_header").outerHeight()+"px");//对齐brand
    jQuery(document).ready(restoreNav);
  }

  function enableSearch(searchInputId){
    nav.searchInputId = searchInputId;
    var searchDom = jQuery('#'+searchInputId).parent().parent();
    searchDom.show();
    searchDom.removeClass("sidebar-search-open");
    searchDom.find(".input-group-append .btn").click(function (event) {
      event.preventDefault();
      nav.toggleSearchResults();
    });
    jQuery(document).on('keyup', '#'+searchInputId, function (event) {
      setTimeout(function () {
        var searchValue=jQuery('#'+nav.searchInputId).val().toLowerCase();
        if(!searchValue || searchValue.length<2){
          nav.closeSearchResults();
        }else{
          nav.search(searchValue, 7);
        }
      }, 100);
    });
  }

  function openMenu(obj,target,iframe){
    return nav.openMenu(obj,target,iframe);
  }

  function changeNavSidebarTheme(theme){
    if(theme == '--') return;
    if(localStorage) localStorage.setItem("beangle.ems.nav_sidebar_theme",theme);
    jQuery("#nav_siderbar_theme_"+theme).prop ("checked", true);
    if(theme=="dark"){
      jQuery('#main_siderbar').removeClass("sidebar-light-lightblue").addClass("sidebar-dark-primary");
      jQuery('#control_sidebar').removeClass("control-sidebar-light").addClass("control-sidebar-dark");
    }else{
      jQuery('#main_siderbar').removeClass("sidebar-dark-primary").addClass("sidebar-light-lightblue");
      jQuery('#control_sidebar').removeClass("control-sidebar-dark").addClass("control-sidebar-light");
    }
  }

  function changeFontSize(font_size){
    if(font_size == "--") return;
    jQuery("#control_sidebar input[name=root_font_size]").each(function(i,a){ if(jQuery(a).val()==font_size) jQuery(a).prop("checked",true)})
    if(localStorage) localStorage.setItem("beangle.ems.root_font_size",font_size);
    document.documentElement.style.setProperty("font-size",font_size);
    jQuery("#main_siderbar .brand-link").css("height",jQuery("#main_header").outerHeight()+"px");//对齐brand
  }

  function changeTheme(theme){
    if(theme){
      if(typeof theme == "string"){
        theme = JSON.parse(theme)
      }
      applyTheme(theme)
      setLocal("beangle.ems.theme",JSON.stringify(theme))
    }else{
      applyTheme(nav.theme)
      setLocal("beangle.ems.theme",null)
    }
  }

  function applyTheme(theme){
    if(typeof theme =="string"){
      theme = JSON.parse(theme)
    }
    var r = document.querySelector(':root');
    r.style.setProperty("--primary-color",theme.primaryColor)
    r.style.setProperty("--navbar-bg-color",theme.navbarBgColor)
    r.style.setProperty("--search-bg-color",theme.searchBgColor)
    r.style.setProperty("--gridbar-bg-color",theme.gridbarBgColor)
    r.style.setProperty("--grid-border-color",theme.gridBorderColor)
    jQuery("#theme_primaryColor").val(theme.primaryColor)
    jQuery("#theme_navbarBgColor").val(theme.navbarBgColor)
    jQuery("#theme_searchBgColor").val(theme.searchBgColor)
    jQuery("#theme_gridbarBgColor").val(theme.gridbarBgColor)
    jQuery("#theme_gridBorderColor").val(theme.gridBorderColor)
  }
  function restoreNav(){
    var menuHref = null;
    var groupId = null;
    var menuLoc = null;
    if( document.location.hash && document.location.hash.startsWith("#/")){
      menuHref = document.location.origin + document.location.hash.substring(1);
    }

    if(menuHref){
      var menuLoc = nav.locateMenu(menuHref);
      if(menuLoc){
        nav.displayGroupMenus(menuLoc.group.id,menuLoc.app.id,menuLoc.menu);
      }
    }
    if(!menuLoc && sessionStorage){
      groupId = sessionStorage.getItem("beangle.ems.nav_group_id")
      menuHref = sessionStorage.getItem("beangle.ems.nav_menu_href");
      if(menuHref) {
        menuLoc = nav.locateMenu(menuHref);
        if(menuLoc){
          nav.displayGroupMenus(menuLoc.group.id,menuLoc.app.id,menuLoc.menu);
        }
      }else if(groupId){
        nav.displayGroupMenus(groupId);
      }
    }
    if(!menuLoc && nav.welcomeUrl){
      bg.Go(nav.welcomeUrl,'main');
    }
  }

  function saveNavState(){
    if(sessionStorage){
      if(nav.currentGroupId) sessionStorage.setItem("beangle.ems.nav_group_id",nav.currentGroupId);
      if(nav.currentMenuHref) sessionStorage.setItem("beangle.ems.nav_menu_href",nav.currentMenuHref);
    }
  }

  function clearNavState(){
    if(sessionStorage){
      nav.currentGroupId=0;nav.currentMenuHref=null;
      sessionStorage.removeItem("beangle.ems.nav_group_id");
      sessionStorage.removeItem("beangle.ems.nav_menu_href");
    }
  }

  jQuery(window).bind("unload",saveNavState);
  exports.createNav=createNav;
  exports.changeGroup=changeGroup;
  exports.createProfileNav=createProfileNav;
  exports.fetchMessages=fetchMessages;
  exports.setup=setup;
  exports.enableSearch=enableSearch;
  exports.openMenu=openMenu;
  exports.changeNavSidebarTheme=changeNavSidebarTheme;
  exports.changeFontSize=changeFontSize;
  exports.clearNavState=clearNavState;
  exports.setWelcomeUrl=function(url){nav.setWelcomeUrl(url);}
  exports.getNav=function(){return nav;}
  exports.changeTheme=changeTheme;
})));
