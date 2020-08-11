[@b.head/]
[#include "/org/beangle/ems/portal/nav/local.ftl"/]
<style>
.nav > li > select {
    position: relative;
    vertical-align:middle;
    margin-top:10px
}
.nav > li > label {
    position: relative;
    vertical-align:middle;
    margin-top:16px;
    border:false
}
</style>
<script>
  function changeDefaultApp(appId){
     this.location="${b.url('!index')}"+"?app.id="+appId
  }
</script>

[@displayFrame appName=appName apps=apps topMenus=menus]
<ul class="nav navbar-nav" style="float:right">
      <li><label for="default_app_select">默认应用:</label>[@b.select items=apps id="default_app_select" name="app.id" value=appId option=r"${item.name} ${item.title}" onchange="changeDefaultApp(this.value)"/]</li>
      <li>[@b.a href="!logout" ]<span class="glyphicon glyphicon-user"></span>退出[/@]</li>
</ul>
[/@]
