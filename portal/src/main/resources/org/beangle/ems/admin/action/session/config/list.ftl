[#ftl]
[@b.head/]

[@b.grid items=sessionConfigs var="sessionConfig"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col width="10%" property="category.name" title="用户分类"/]
    [@b.col width="18%" property="ttiMinutes" title="过期时长(分)"/]
    [@b.col width="14%" property="concurrent" title="多重会话数"/]
    [@b.col width="14%" property="capacity" title="最大会话容量"/]
    [@b.col width="20%" property="checkConcurrent" title="是否检查多重会话"/]
    [@b.col width="20%" property="checkCapacity" title="是否检查系统会话容量"/]
  [/@]
[/@]
[@b.foot/]
