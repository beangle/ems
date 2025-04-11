[#ftl]
[@b.head/]
  [@b.grid items=templates var="template"]
    [@b.gridbar]
      bar.addItem("${b.text("action.new")}",action.add());
      bar.addItem("${b.text("action.edit")}",action.edit());
      bar.addItem("${b.text("action.delete")}",action.remove());
      bar.addItem("复制",action.single('copy'));
    [/@]
    [@b.row]
      [@b.boxcol/]
      [@b.col title="业务类型" property="business.name" width="13%"/]
      [@b.col title="类型" property="todo" width="10%"]
        ${template.todo?string('代办提醒','办结通知')}
      [/@]
      [@b.col title="模板名称" property="name"  width="15%"]
        [@b.a href="!info?id=${template.id}" title="查看详情"]${template.name}[/@]
      [/@]
      [@b.col title="模板标题" property="title"]
        [@b.a href="!info?id=${template.id}" title="查看详情"]${template.title}[/@]
      [/@]
      [@b.col title="发送时机" property="delayMinutes" width="10%"]
        [#if template.delayMinutes==0]立即发送[#else]延迟${template.delayMinutes}分钟[/#if]
      [/@]
    [/@]
  [/@]
