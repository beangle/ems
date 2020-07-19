[#ftl attributes={'content_type':'application/xml'}]
[#macro displayMenus menu]
  <menu indexno="${menu.indexno}" entry="${(menu.entry.name)!}" title="${menu.title}" name="${menu.name}" params="${menu.params!}" enabled="${menu.enabled?c}">
     [#if menu.resources?size>0]
     <resources>
       [#list menu.resources as r]
       <resource name="${r.name}" title="${r.title}" scope="${r.scope}" enabled="${r.enabled?c}"/>
       [/#list]
     </resources>
     [/#if]
     [#if menu.children?size>0]
       <children>
         [#list menu.children as m]
           [@displayMenus m/]
         [/#list]
       </children>
     [/#if]
  </menu>
[/#macro]
<?xml version="1.0" encoding="UTF-8"?>
<profile>
 [#list menus as m]
   [@displayMenus m/]
 [/#list]
</profile>
