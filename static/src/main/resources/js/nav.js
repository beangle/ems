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
    this.menuTempalte='<li class="nav-item"><a class="nav-link" onclick="return bg.Go(this,\'{menu.target}\')" href="{menu.entry}" target="{menu.target}" ><i class="nav-icon {icon_class}"></i><p>{menu.title}</p></a></li>';
    if(document.getElementById('main').tagName!='DIV'){
      this.menuTempalte='<li class="nav-item"><a class="nav-link" target="main" href="{menu.entry}"><i class="nav-icon fa fa-circle-o"></i><p>{menu.title}</p></a></li>';
    }
    this.foldTemplate='<li class="nav-item has-treeview {open_class}"><a class="nav-link {active_class}" href="javascript:void(0)"><i class="nav-icon fa fa-list"></i><p>{menu.title}<i class="nav-icon fa fa-angle-left right"></i></p></a><ul class="nav nav-treeview" id="menu{menu.id}"></ul></li>'
    this.appFoldTemplate='<li class="nav-item has_treeview {open_class}"><a class="nav-link {active_class}" href="javascript:void(0)"><i class="nav-icon fa fa-list"></i><p>{app.title}<i class="nav-icon fa fa-angle-left right"></i></p></a><ul class="nav nav-treeview" id="menu_app{app.id}"></ul></li>'
    if(!this.app.navStyle){
     this.app.navStyle="unkown";
    }
    this.collectApps();
    this.processMenus();
    if(!this.sysName){
      this.sysName=this.app.title;
    }
    jQuery("#"+this.menuDomId).addClass("sidebar-menu");
  }

  Nav.prototype={
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
          app.embeddable=true;
          if(app.navStyle != this.app.navStyle){
            app.embeddable=false;
          }
          if(!ems.sameDomain(this.app.base,app.base)){
            app.embeddable=false;
          }
        }
      }
    },
    search : function(name,limit){
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
          var result = {'name':menu.title,'link':menu.entry,'path':resultPath,'target':menu.target}
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
        if(menu.entry  && !menu.entry.startsWith("http")){
           if(app.embeddable) menu.target="main";
           else menu.target="_blank";
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
        if(menu.menus){//menu is an app
          var appItem = this.appFoldTemplate.replace('{app.id}',menu.app.id);
          appItem = appItem.replace('{app.title}',menu.app.title);
          appItem = appItem.replace('{open_class}',(openMenuId==menu.app.id)?"menu-open":"");
          appItem = appItem.replace('{active_class}',(openMenuId==menu.app.id)?"active":"");
          jqueryElem.append(appItem);
          this.createMenus(jQuery('#menu_app'+menu.app.id),menu.menus);
        }else if(menu.children){//fold
          menuItem = this.foldTemplate.replace('{menu.id}',menu.id);
          menuItem = menuItem.replace('{menu.title}',menu.title);
          menuItem = menuItem.replace('{open_class}',(openMenuId==menu.id)?"menu-open":"");
          menuItem = menuItem.replace('{active_class}',(openMenuId==menu.id)?"active":"");
          jqueryElem.append(menuItem);
          this.createMenus(jQuery('#menu'+menu.id),menu.children);
        }else{//menu
          menuItem = this.menuTempalte.replace('{menu.id}',menu.id);
          menuItem = menuItem.replace('{menu.title}',menu.title);
          menuItem = menuItem.replace('{icon_class}',this.getIconClass(menu.title));
          menuItem = menuItem.replace('{menu.entry}',menu.entry);
          menuItem = menuItem.replace('{menu.target}',menu.target);
          jqueryElem.append(menuItem);
        }
      }
    },
    activate : function(){
      var that=this;
      //FIXME treeview someding missing domcument onloading events
      if(!jQuery("#"+this.menuDomId).data("lte.treeview")){
         jQuery.fn.Treeview.call(jQuery("#"+this.menuDomId),"init");
      }
      jQuery("#"+this.menuDomId+" li a").click(function() {
        if(this.href=="javascript:void(0)"){
          jQuery(this).parent('li').siblings().each(function (i,li){
              jQuery(li).removeClass('menu-open menu-is-opening');
              jQuery(li).children('ul').hide();
              jQuery(li).children('a').removeClass('active');
            }
          );
          var jThis=jQuery(this);
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
   * 添加app导航,是app内部菜单之外的所有app的展示pannel
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

  /**
   * 展现全部domain内所有group的菜单（最全的）
   */
  function DomainNav(nav){
    this.nav=nav;
    this.groupTemplate='<li class="nav-item"><a class="nav-link {active_class}" href="javascript:void(0)" id="group_{group.id}">{group.title}</a></li>';
    this.portalTemplate='<li class="nav-item"><a href="{app.url}" class="nav-link" target="_self">{app.title}</a></li>';
    this.dropdownGroupNavTemplate='<a href="javascript:void(0)"  class="dropdown-item {active_class}" id="group_{group.id}">{group.title}</a>'
    /**
     * 添加顶层groups
     */
    this.addTopGroups = function(jqueryElem){
      var appItem='';
      var topItemCount=0;
      var topMoreHappened=false;
      jQuery('#appName').html(this.nav.sysName);
      prependApps(jqueryElem,this.nav,this.nav.apps,true)

      for(var i=0;i < this.nav.groups.length; i++){
        var group = this.nav.groups[i];
        topItemCount += 1;
        if(topItemCount == this.nav.maxTopItem && this.nav.groups.length > this.nav.maxTopItem){
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
    }

    this.displayCurrent=function(){
      this.displayGroupMenus(this.nav.currentGroupId);
    }

    this.displayAppMenus=function(appName){
      var groupId=0,appId=0;
      if (appName == this.nav.portal.name){
         groupId = this.nav.portal.group.id;
      }else{
        var apps = this.nav.apps;
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
        groupId = this.nav.groupMenus[0].group.id
      }
      this.displayGroupMenus(groupId,appId)
   }
    /**
     * 显示指定group的menu
     */
    this.displayGroupMenus = function (groupId,appId){
      switchNavActive("#group_"+groupId);
      for(var i=0;i < this.nav.groupMenus.length; i++){
        var groupMenu=this.nav.groupMenus[i];
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
            this.nav.createMenus(jQuery('#'+this.nav.menuDomId),onlyOneAppMenu.menus,openMenuId);
          }else{
            this.nav.createMenus(jQuery('#'+this.nav.menuDomId),groupMenu.appMenus,openMenuId);
          }
          this.nav.activate();
          this.nav.currentGroupId=groupId;
          break;
        }
      }
    }
    /** 菜单查找
     */
    this.search=function(name,limit){
      var results = this.nav.search(name,limit);
      var searchRegExp = new RegExp(name, 'gi');
      var resultDom = jQuery(".sidebar-search-results .list-group");
      resultDom.empty();
      if(results.length ==0){
        var notfound={'name':"找不到结果",'link':'#','path':[],'target':"main"}
        resultDom.append(this.renderSearchItem(searchRegExp,notfound))
      }else{
        for(var i=0;i < results.length;i++){
          var result = results[i];
          resultDom.append(this.renderSearchItem(searchRegExp,result));
        }
      }
      this.openSearchResults();
    }

    this.enableSearch=function(searchInputId){
      this.searchInputId = searchInputId;
      var that = this;
      jQuery(document).on('keyup', '#'+searchInputId, function (event) {
        setTimeout(function () {
          this.search(jQuery('#'+that.searchInputId).val().toLowerCase(), 7);
        }, 100);
      });
    }
    this.openSearchResults = function(){
      var searchDom =jQuery('#'+this.searchInputId).parent().parent();
      searchDom.addClass("sidebar-search-open");
      searchDom.find(".input-group-append .btn i").removeClass("fa-search").addClass("fa-times");
    }
    this.closeSearchResults = function(){
      var searchDom =jQuery('#'+this.searchInputId).parent().parent();
      searchDom.removeClass("sidebar-search-open");
      searchDom.find(".input-group-append .btn i").removeClass("fa-times").addClass("fa-search");
    }
    this.toggleSearchResults = function(){
      var searchDom =jQuery('#'+this.searchInputId).parent().parent();
      if (searchDom.hasClass("sidebar-search-open")) {
        this.closeSearchResults();
      } else {
        this.openSearchResults();
      }
    }
    this.renderSearchItem = function(searchRegExp,result){
      var pathStr = result.path.join(" -> ");
      var name = result.name.replace(searchRegExp, function (str) {
        return "<strong class=\"text-light\">" + str + "</strong>";
      });
      var groupItemElement = jQuery('<a/>', {href: decodeURIComponent(result.link), class: 'list-group-item'  });
      var searchTitleElement = jQuery('<div/>', {class: 'search-title' }).html(name);
      var searchPathElement = jQuery('<div/>', {class: 'search-path'}).html(pathStr);
      groupItemElement.append(searchTitleElement).append(searchPathElement);
      if(result.link != '#') groupItemElement.click(function (){return bg.Go(this,result.target)});
      return groupItemElement;
    }
  }

  /**
   * 显示一个group中的各个app的餐单
   */
  function GroupNav(nav){
    this.nav=nav;
    this.appExternTemplate   ='<li class="nav-item"><a href="{app.url}"  class="nav-link {active_class}" target="_top" id="app_{app.id}">{app.title}</a></li>';
    this.appNavTemplate='<li class="nav-item"><a href="{app.url}"  class="nav-link {active_class}" id="app_{app.id}">{app.title}</a></li>';
    //add app in dropdown
    this.dropdownAppExternTemplate   ='<a href="{app.url}"  class="dropdown-item {active_class}" target="_top">{app.title}</a>';
    this.dropdownAppNavTemplate='<a href="{app.url}"  class="dropdown-item {active_class}" id="app_{app.id}">{app.title}</a>';
    /**
     * 向顶层添加app
     */
    this.addTopApps = function(jqueryElem){
      var topItemCount=0;
      var appItem='';
      var topMoreHappened=false;
      var thisApp=this.nav.app;
      var groupApps=[this.nav.portal];
      //过滤掉非所在group的app
      for(var i=0;i<this.nav.apps.length;i++){
        var app =this.nav.apps[i];
        if(app.name==this.nav.portal.name){
          continue;
        }
        if(app.group && thisApp.group && app.group.id != thisApp.group.id){
          continue;
        }
        groupApps.push(app);
      }
      prependApps(jqueryElem,this.nav,groupApps,true)
      for(var i=0;i<groupApps.length;i++){
        var app = groupApps[i];
        topItemCount += 1;
        if(app.name==this.nav.app.name){
          var appName=app.title;
          if(app.group && app.group.title) appName=app.group.title
          jQuery('#appName').html(appName);
        }
        if(topItemCount == this.nav.maxTopItem && groupApps.length > this.nav.maxTopItem){
          jqueryElem.append('<li class="nav-item dropdown"><a href="#" data-toggle="dropdown" class="dropdown-toggle nav-link">更多...</a><div id="topMore" aria-labelledby="navbarDropdown" class="dropdown-menu"></div><li>');
          topMoreHappened=true;
        }
        if(topMoreHappened){
          jqueryElem = jQuery('#topMore');
        }
        if(app.embeddable){
          if(topMoreHappened){
            appItem = this.dropdownAppNavTemplate.replace('{app.id}',app.id);
          }else{
            appItem = this.appNavTemplate.replace('{app.id}',app.id);
          }
          appItem = appItem.replace('{app.title}',app.title);
          appItem = appItem.replace('{app.url}',this.nav.processUrl(app.url));
          appItem = appItem.replace('{active_class}',app.name==this.nav.app.name?"active":"");
          jqueryElem.append(appItem);
          jQuery("#app_"+app.id).click(function (){changeApp(this);return false;})
        }else{
          if(topMoreHappened){
            appItem = this.dropdownAppExternTemplate.replace('{app.id}',app.id);
          }else{
            appItem = this.appExternTemplate.replace('{app.id}',app.id);
          }
          appItem = appItem.replace('{app.title}',app.title);
          appItem = appItem.replace('{app.url}',this.nav.processUrl(app.url));
          appItem = appItem.replace('{active_class}',app.name==this.nav.app.name?"active":"");
          jqueryElem.append(appItem);
        }
      }
    }

    /**
     * 显示Group内的指定appId的菜单
     */
    this.displayAppMenus=function(appId){
      if(!appId){
        console.log("display menus need appId");
        return;
      }
      var targetApp=null
      for(var i=0;i<this.nav.apps.length;i++){
        if(this.nav.apps[i].id==appId){
          targetApp=this.nav.apps[i];
          break;
        }
      }
      if(targetApp){
        switchNavActive("#app_"+appId);
        var appMenu=this.nav.appMenus[targetApp.name];
        if(appMenu){
          var openMenuId=targetApp.id;
          if(appMenu.length>0){
            var first=appMenu[0];
            if(!first.menus && !first.children){
               appMenu=[{app:targetApp,menus:appMenu}];
            }else{
               openMenuId=first.id;
            }
          }
          this.nav.createMenus(jQuery('#'+this.nav.menuDomId),targetApp,appMenu,openMenuId);
          this.nav.activate();
        }else{
          console.log("Cannot find menu for app "+targetApp.name);
        }
      }else{
        console.log("Cannot find app named:"+appName);
      }
    }

    this.displayCurrent=function(){
      this.displayAppMenus(this.nav.app.id);
    }
  }
  /**
   * 导航栏和菜单栏都是app中的内容
   */
  function AppNav(nav){
    this.nav=nav;
    this.topMenuTemplate='<li class="nav-item"><a  id="topMenu_{menu.idx}" href="javascript:void(0)"  class="nav-link {active_class}">{menu.title}</a></li>';
    this.dropdownTopMenuTemplate='<a id="topMenu_{menu.idx}" href="javascript:void(0)"  class="dropdown-item {active_class}">{menu.title}</a>';

    this.addTopMenus=function(jqueryElem){
      prependApps(jqueryElem,this.nav,this.nav.apps,false)
      jqueryElem.empty();
      for(var i=0;i<nav.apps.length;i++){
        if(nav.apps[i].name == nav.app.name){
          jQuery('#appName').html(nav.apps[i].title);
        }
      }
      var topItemCount=0;
      var appendHtml='';
      var menus = this.nav.appMenus[this.nav.app.name];
      for(var i=0;i<menus.length;i++){
        var menu = menus[i];
        if(!menu.children || menu.children.length==0){
          continue;
        }
        topItemCount +=1;
        if(topItemCount == this.nav.maxTopItem){
          jqueryElem.append('<li class="nav-item dropdown"><a href="#" data-toggle="dropdown" class="dropdown-toggle nav-link">更多...</a><div id="topMore" class="dropdown-menu"></div><li>');
        }
        if(topItemCount >= this.nav.maxTopItem ){
          jqueryElem = jQuery('#topMore');
          appendHtml = this.dropdownTopMenuTemplate.replace('{menu.title}',menu.title);
        }else{
          appendHtml = this.topMenuTemplate.replace('{menu.title}',menu.title);
        }
        appendHtml = appendHtml.replace('{menu.idx}',i);
        appendHtml = appendHtml.replace('{menu.idx}',i);
        appendHtml = appendHtml.replace('{active_class}',(i==0)?"active":"");
        jqueryElem.append(appendHtml);
        jQuery("#topMenu_"+i).click(function(){changeMenu(this);return false;});
      }
    }

    this.displayTopMenus=function(idx){
      switchNavActive("#topMenu_"+idx);
      var menus=this.nav.appMenus[this.nav.app.name]
      var children = menus[idx].children;
      var openMenuId=0;
      if(children){
        //如果只有有个顶级目录，则也展现等级目录，否则仅展示下级目录
        if(children.length>0 && (!children[0].children || children[0].children.length==0)){
          menus=[menus[idx]];
        }else{
          menus=children;
        }
        if(menus.length>0) openMenuId = menus[0].id;
      }
      this.nav.createMenus(jQuery('#'+this.nav.menuDomId),this.nav.app,menus,openMenuId);
      this.nav.activate();
    }
  }

  var navMenu ={};

  function createDomainNav (app,portal,domainMenus,params,displayFirstGroup){
    var nav= new Nav(app,portal,domainMenus,params);
    navMenu = new DomainNav(nav);
    navMenu.addTopGroups(jQuery('#'+nav.navDomId));
    if(displayFirstGroup && nav.groups.length>0){
      navMenu.displayGroupMenus(nav.groups[0].id);
    }
    return navMenu;
  }

  function createGroupNav(app,portal,domainMenus,params){
    var nav= new Nav(app,portal,domainMenus,params);
    var group = new GroupNav(nav);
    group.addTopApps(jQuery('#'+nav.navDomId));
    group.displayAppMenus(nav.app.id);
    navMenu=group;
  }

  function createAppNav(app,portal,domainMenus,params){
    var nav= new Nav(app,portal,domainMenus,params);
    var appNav= new AppNav(nav);
    appNav.addTopMenus(jQuery('#'+nav.navDomId));
    appNav.displayTopMenus(0);
    navMenu=appNav;
  }

  /**
   * 切换app的全局函数
   * @param id
   * @param name
   * @returns
   */
  function changeApp(ele){
    var id=ele.id
    navMenu.displayAppMenus(id.substring("app_".length));
  }

  /**
   * 切换group的全局函数
   * @param id
   * @param name
   * @returns
   */
  function changeGroup(ele){
    var id=ele.id
    navMenu.displayGroupMenus(id.substring("group_".length));
  }

  function changeMenu(ele){
    var id=ele.id
    navMenu.displayTopMenus(id.substring("topMenu_".length));
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

  function setup (params) {
    jQuery("body").addClass("sidebar-mini layout-fixed text-sm");
    fetchMessages(params);
    jQuery("#sticky_header").on("click",function(event){
      if(this.checked){
        jQuery('.main-header').addClass("sticky-top");
      }else{
        jQuery('.main-header').removeClass("sticky-top");
      }
    });
    jQuery("#page_size_selector").on("change",function(event){
       beangle.createCookie("pageSize",this.value,100);
    });
  }

  function enableSearch (searchInputId){
    navMenu.searchInputId = searchInputId;
    var searchDom = jQuery('#'+searchInputId).parent().parent();
    searchDom.show();
    searchDom.removeClass("sidebar-search-open");
    searchDom.find(".input-group-append .btn i").click(function (event) {
        event.preventDefault();
        navMenu.toggleSearchResults();
    });
    jQuery(document).on('keyup', '#'+searchInputId, function (event) {
      setTimeout(function () {
        var searchValue=jQuery('#'+navMenu.searchInputId).val().toLowerCase();
        if(!searchValue || searchValue.length<2){
          navMenu.closeSearchResults();
        }else{
          navMenu.search(searchValue, 7);
        }
      }, 100);
    });
  }

  function toggleTopBar(){
    var bar=jQuery("#"+this.navMenu.nav.navDomId)
    if(bar.is(":hidden")){
      bar.css("margin","50px 0px 0px 0px")
      bar.show();
    }else{
      bar.css("margin","0px 0px 0px 0px")
      bar.hide();
    }
  }

  exports.createDomainNav=createDomainNav;
  exports.createGroupNav=createGroupNav;
  exports.createAppNav=createAppNav;
  exports.changeGroup=changeGroup;
  exports.createProfileNav=createProfileNav;
  exports.fetchMessages=fetchMessages;
  exports.setup=setup;
  exports.enableSearch=enableSearch;
  exports.toggleTopBar=toggleTopBar;
})));
