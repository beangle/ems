[#ftl]
[@b.head/]
[#assign statusMap={'1':'未读','2':'已读','3':'删除'}/]
<div class="card card-primary card-outline">
   [@b.form  name="messageListForm" id="messageListForm" action="!sentList"]
   [#list Parameters as k,v]
   [#if k!="message.id" && k!="message.title" ]
   <input name="${k}" value="${v}" type="hidden"/>
   [/#if]
   [/#list]
    <div class="card-header">
      <h3 class="card-title">已发送消息</h3>
      <div class="card-tools float-right">
        <div class="input-group input-group-sm">
          <input type="text" id="messageSearchBox" name="message.title" value="${Parameters['message.title']!}" class="form-control input-sm" placeholder="查询消息" >
          <div class="input-group-append"><div class="btn btn-primary"><i class="fas fa-search"></i></div></div>
        </div>
      </div>
    </div>
   [/@]

    <div class="card-body p-0">
      <div class="mailbox-controls">
        <button type="button" class="btn btn-default btn-sm"  onclick="gotoPage(${messages.pageIndex})"><i class="fas fa-sync-alt"></i></button>
        <div class="float-right">
          [#if messages.totalItems==0]
          0-0/0
          [#else]
          ${(messages.pageIndex-1)*messages.pageSize+1}-${(messages.pageIndex-1)*messages.pageSize+messages.items.size}/${messages.totalItems}
          [/#if]
          <div class="btn-group">
            <button type="button" class="btn btn-default btn-sm" [#if messages.hasPrevious] onclick="gotoPage(${messages.pageIndex-1})"[#else] disabled="disabled"[/#if]><i class="fa fa-chevron-left"></i></button>
            <button type="button" class="btn btn-default btn-sm" [#if messages.hasNext] onclick="gotoPage(${messages.pageIndex+1})"[#else] disabled="disabled"[/#if]><i class="fa fa-chevron-right"></i></button>
          </div>
        </div>
      </div>

      <div class="table-responsive mailbox-messages">
        <table class="table table-hover table-striped" id="messageGrid">
          <tbody>
          [#list messages as message]
          <tr>
            <td class="mailbox-name"  width="13%">[@b.a href="!info?id=${message.id}"]${message.recipient.name}[/@]</td>
            <td class="mailbox-subject" width="65%">
               <b>${message.title}</b> - [#if message.contents?length>30]${message.contents?substring(0,30)?html}...[#else]${message.contents?html}[/#if]
            </td>
            <td  width="7%">${statusMap[message.status?string]}</td>
            <td class="mailbox-date" style="font-size:0.8em"  width="15%">${message.sentAt?string('yy-MM-dd HH:mm')}</td>
          </tr>
          [/#list]
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <script>
    function toggleAll(){
      jQuery("#messageGrid [type='checkbox']").each(function(){
          var ele=jQuery(this);
          if(!ele.is(":checked")){
            ele.prop("checked",true);
            jQuery(this).parent("tr").addClass("griddata-selected");
          }else{
              ele.prop("checked",false);
              jQuery(this).parent("tr").removeClass("griddata-selected");
          }
        });
      }
      function gotoPage(pageIndex){
        if(pageIndex){
          bg.form.addInput(document.messageListForm,'pageIndex',pageIndex);
        }
        bg.form.submit(document.messageListForm);
      }

      $("#messageSearchBox").keydown(function (e) {
       if (e.keyCode == 13) {
        bg.form.submit(document.messageListForm);
       }
      });
  </script>
[@b.foot/]
