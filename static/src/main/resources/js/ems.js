(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
  typeof define === 'function' && define.amd ? define(['exports'], factory) :
  (global = global || self, factory(global.ems = {}));
}(this, function (exports) { 'use strict';

  var config = {profiles:[],profile:{},api:""};

  function processProfileUrl() {
    for(var i=0;i < config.profiles.length;i++){
      var profile = config.profiles[i];
      if(!profile.url){
        profile.url = (location.origin + location.pathname +"?contextProfileId=" +profile.id)
      }
    }
  }

  function init(profiles,cookie){
    var profile=null;
    if(cookie && cookie.profile){
      for(var i=0;i<profiles.length;i++){
        var p = profiles[i];
        if(p.id == cookie.profile){
          profile=p;
          break;
        }
      }
    }
    if(!profile){
      profile = profiles[0];
    }
    config.profiles=profiles;
    config.profile=profile;
    processProfileUrl();
  }

  function hostName (u1){
    var slashIdx = u1.indexOf('//');
    if(-1==slashIdx){
      slashIdx=0;
    }else{
      slashIdx += 2;
    }
    var endIdx= u1.indexOf(':',slashIdx)
    if(-1 == endIdx){
      endIdx= u1.indexOf('/',slashIdx)
    }
    if(-1 == endIdx){
      endIdx= u1.length
    }
    return u1.substring(slashIdx,endIdx);
  }

  function sameDomain (u1,u2){
    return this.hostName(u1)== this.hostName(u2);
  }

  exports.config = config;
  exports.hostName=hostName
  exports.sameDomain=sameDomain;
  exports.init=init;
}));
