[#ftl]
[@b.head/]
    <section class="content-header">
      <h1>
        我的消息
        <small>${stats['1']!0} 新消息</small>
      </h1>
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="#"><i class="fa fa-dashboard"></i> 首页</a></li>
        <li class="breadcrumb-item active">消息</li>
      </ol>
    </section>

    <section class="content">
      <div class="row">
        <div class="col-md-3">
          [@b.a href="!editNew" target="messageList" class="btn btn-primary btn-block margin-bottom"]发送新消息[/@]
          <div class="card">
           <div class="card-header">
              <h3 class="card-title">文件夹</h3>
            </div>
            <div class="card-body p-0">
              <ul class="nav nav-pills flex-column" id="msgboxList">
                <li class="nav-item active">
                [@b.a class="nav-link" href="!search?message.status=1" target="messageList"]
                  <i class="fas fa-inbox" aria-hidden="true"></i>新消息
                  [#if (stats['1']!0)>0]
                  <span class="badge bg-primary float-right">${stats['1']!0}</span>
                  [/#if]
                [/@]
                </li>
                <li class="nav-item">
                   [@b.a class="nav-link" href="!search?message.status=2" target="messageList"]
                    <i class="fa fa-envelope-open" aria-hidden="true"></i>已读
                    [#if (stats['2']!0)>0]
                    <span class="badge bg-primary float-right">${stats['2']!0}</span>
                    [/#if]
                   [/@]
                </li>
                <li class="nav-item">[@b.a href="!sentList"  class="nav-link" target="messageList"]
                  <i class="far fa-envelope"></i> 已发送
                   [/@]
                </li>
                <li class="nav-item">
                    [@b.a href="!search?message.status=3"  class="nav-link" target="messageList"]
                    <i class="fa fa-trash" aria-hidden="true"></i>垃圾箱
                    [#if (stats['3']!0)>0]
                    <span class="badge bg-primary float-right">${stats['3']!0}</span>
                    [/#if]
                    [/@]
                </li>
              </ul>
            </div>
          </div>
        </div>
      [@b.div id="messageList" href="!search?message.status=1"  class="col-md-9"/]
      </div>
    </section>
    <script>
      jQuery("#msgboxList > li > a").click(function(){
         jQuery(this).parent().siblings().each(function(i,li){jQuery(li).children("a").removeClass("active")});
         jQuery(this).addClass("active")
      });
    </script>
[@b.foot/]
