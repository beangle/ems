[#ftl]
[@b.head/]
[@b.toolbar title="用户权限面板"]bar.addClose("${b.text("action.close")}");[/@]
<style>
 .list-group-unbordered > .list-group-item {
   border-left: 0;
   border-right: 0;
   border-radius: 0;
   padding-left: 0;
   padding-right: 0;
}
</style>
<div class="container">
  <div class="row">
    <div class="col-md-3">
      [#include "panels/account.ftl"/]
      [#include "panels/profile.ftl"/]
    </div>

    <div class="col-md-9">
      [#include "panels/online.ftl"/]
      [#include "panels/menus.ftl"/]
      [#include "panels/events.ftl"/]
    </div>

  </div>
</div>

[@b.foot/]
