[#ftl]
[@b.head/]
[#assign businesses = stat?keys?sort_by('code')/]
[#assign total = 0/]
[#list businesses as business]
  [#assign total = total + stat.get(business)/]
[/#list]
<div class="container-fluid">
    <section class="content-header">
      <div class="row mb-2">
        <div class="col-sm-6">
          <h5 class="m-0 text-dark">我的代办 <small>${total}新代办</small></h5>
        </div>
        <div class="col-sm-6">
          <ol class="breadcrumb  float-sm-right">
            <li class="breadcrumb-item"><i class="fas fa-tachometer-alt" style="margin-top: 6px;"></i> 个人中心</li>
            <li class="breadcrumb-item active">代办</li>
          </ol>
        </div>
      </div>
    </section>
    <section class="content">
      <div class="row">
        <div class="col-md-2">
          <div class="card card-primary card-outline">
            <div class="card-header">
              <h3 class="card-title">代办业务</h3>
            </div>
            <div class="card-body">
              <ul class="nav nav-pills flex-column" id="businessList">
                <li class="nav-item">
                  [@b.a class="nav-link active" href="!search" target="todolist"]
                    <i class="fas fa-inbox" aria-hidden="true"></i>全部
                    <span class="badge bg-primary float-right">${total}</span>
                  [/@]
                </li>
                [#list businesses as business]
                <li class="nav-item">
                  [@b.a class="nav-link" href="!search?todo.business.id="+business.id target="todolist"]
                    <i class="fas fa-inbox" aria-hidden="true"></i>${business.name}
                    <span class="badge bg-primary float-right">${stat.get(business)}</span>
                  [/@]
                </li>
                [/#list]
                <li class="nav-item acitve">
                  [@b.a class="nav-link" href="!done" target="todolist"]
                    <i class="fas fa-inbox" aria-hidden="true"></i>已办结
                    <span class="badge bg-primary float-right">${doneCount}</span>
                  [/@]
                </li>
              </ul>
            </div>
          </div>
        </div>
        [#if businesses?size>0]
          [@b.div id="todolist" href="!search?todo.business.id="+(businesses?first.id!0)  class="col-md-10"/]
        [#else]
          [@b.div id="todolist" href="!search"  class="col-md-10"/]
        [/#if]
      </div>
    </section>
    <script>
      jQuery("#businessList > li > a").click(function(){
         jQuery(this).parent().siblings().each(function(i,li){jQuery(li).children("a").removeClass("active")});
         jQuery(this).addClass("active")
      });
    </script>
</div>
[@b.foot/]
