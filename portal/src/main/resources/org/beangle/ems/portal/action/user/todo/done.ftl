[#ftl]
[@b.head/]
<div class="card card-primary card-outline">
   [@b.form name="todoListForm" id="todoListForm" action="!done"]
   [#list Parameters as k,v]
   [#if k!="todo.id" && k!="todo.contents" && k != '_']
   <input name="${k}" value="${v}" type="hidden"/>
   [/#if]
   [/#list]
    <div class="card-header">
      <h3 class="card-title">${(business.name)!'全部已办结'}</h3>
      <div class="card-tools">
        <div class="input-group input-group-sm">
          <input type="text" id="todoSearchBox" name="todo.contents" value="${Parameters['todo.contents']!}" class="form-control input-sm" placeholder="查询代办" >
          <div class="input-group-append"><div class="btn btn-primary"><i class="fas fa-search" onclick="bg.form.submit(document.todoListForm);"></i></div></div>
        </div>
      </div>
    </div>
   [/@]

    <div class="card-body">
      <div class="mailbox-controls">
        <div class="btn-group">
          <button type="button" class="btn btn-default btn-sm"  onclick="gotoPage(${todoes.pageIndex})"><i class="fas fa-sync-alt"></i></button>
        </div>
        <div class="float-right">
          [#if todoes.totalItems==0]
          0-0/0
          [#else]
          ${(todoes.pageIndex-1)*todoes.pageSize+1}-${(todoes.pageIndex-1)*todoes.pageSize+todoes.items.size}/${todoes.totalItems}
          [/#if]
          <div class="btn-group">
            <button type="button" class="btn btn-default btn-sm" [#if todoes.hasPrevious] onclick="gotoPage(${todoes.pageIndex-1})"[#else] disabled="disabled"[/#if]><i class="fa fa-chevron-left"></i></button>
            <button type="button" class="btn btn-default btn-sm" [#if todoes.hasNext] onclick="gotoPage(${todoes.pageIndex+1})"[#else] disabled="disabled"[/#if]><i class="fa fa-chevron-right"></i></button>
          </div>
        </div>
      </div>

      <div class="table-responsive mailbox-todos">
        <table class="table table-hover table-striped" id="todoGrid">
          <tbody>
          [#list todoes as todo]
          <tr>
            <td width="7%">${todo_index +1}</td>
            <td class="mailbox-subject" >
               [#if !business??]<b>${todo.business.name}</b> - [/#if] <b>${todo.title}</b> - ${todo.contents?html}
            </td>
            <td class="mailbox-name" width="13%"><a href="${todo.url}" target="_blank">查看</a></td>
            <td class="mailbox-date" width="15%">${todo.updatedAt?string('yy-MM-dd HH:mm')}</td>
            <td class="mailbox-date" width="15%">${todo.completeAt?string('yy-MM-dd HH:mm')}</td>
          </tr>
          [/#list]
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <script>
      function gotoPage(pageIndex){
        if(pageIndex){
          bg.form.addInput(document.todoListForm,'pageIndex',pageIndex);
        }
        bg.form.submit(document.todoListForm);
      }

      $("#todoSearchBox").keydown(function (e) {
       if (e.keyCode == 13) {
          bg.form.submit(document.todoListForm);
          return false;
       }
      });
      var page_msg = bg.page("${request.requestURI}",'');
      page_msg.addParams('${b.paramstring}').orderBy("${Parameters['orderBy']!('null')}");
      page_msg.pageInfo(${todoes.pageIndex},${todoes.pageSize},${todoes.totalItems});
      page_msg.formid='todoListForm';
      document.todoListForm.target="";
  </script>
[@b.foot/]
