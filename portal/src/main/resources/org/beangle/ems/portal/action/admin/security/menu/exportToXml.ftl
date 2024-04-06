[#ftl attributes={'content_type':'application/xml'}]
[#macro menu_attrs menu]
indexno="${menu.indexno}" name="${menu.name}" enName="${menu.enName}" [#if menu.entry??] entry="${menu.entry.name}"[/#if][#if menu.params??] params="${menu.params}"[/#if][#if menu.fonticon??] fonticon="${menu.fonticon}"[/#if][#if !menu.enabled] enabled="false"[/#if][#if menu_resources?size>0] resources="${menu_resources?join(',')}"[/#if] [#t/]
[/#macro]
[#macro displayMenus menu]
  [#assign menu_resources=[]/]
  [#list menu.resources as r]
    [#if !menu_resources?seq_contains(r.name)]
      [#if !menu.entry?? || r != menu.entry]
      [#assign menu_resources = menu_resources + [r.name]/]
      [/#if]
    [/#if]
  [/#list]
  [#if menu.children?size>0]
  <menu [@menu_attrs menu/]>
       <children>
         [#list menu.children as m]
           [@displayMenus m/]
         [/#list]
       </children>
  </menu>
  [#else]
  <menu  [@menu_attrs menu/]/>
  [/#if]
[/#macro]
<?xml version="1.0" encoding="UTF-8"?>
<profile>
  <resources>
    [#list resources as r]
    <resource name="${r.name}" title="${r.title}" [#if r.scope?string!='Private'] scope="${r.scope}"[/#if][#if !r.enabled] enabled="false"[/#if]/>
    [/#list]
  </resources>
 [#list menus as m]
   [@displayMenus m/]
 [/#list]
</profile>
