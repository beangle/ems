<script>
 beangle.load(["jquery-ui","chosen","bui-ajaxchosen","jquery-colorbox","my97"]);
    bg.load(["sj-jquery-subscribe","sj-jquery","sj-jquery-ui"],function (){
        jQuery.struts2_jquery.version="3.6.1";
        jQuery.scriptPath ="${b.static_base}/struts2-jquery/3.6.1/"
        jQuery.struts2_jquerySuffix = "";
        jQuery.ajaxSettings.traditional = true;
        jQuery.ajaxSetup ({cache: false});
     });
   beangle.getContextPath=function(){
      return "${base}";
   }
   var App = {contextPath:'${base}'}
</script>
