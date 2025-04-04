[#ftl]
[@b.head/]

[@b.form action=b.rest.save(message) name="newMessageForm" onsubmit="validateMessage"]
<input type="hidden" value="&message.status=1" name="_params">
<div class="card card-primary card-outline">
            <div class="card-header">
              <h3 class="card-title">编写新消息</h3>
            </div>
            <div class="card-body">
              <div class="form-group">
                <input  name="recipient.code" class="form-control" placeholder="发送给: 填写接受者的账号" value="${Parameters['recipient.code']!}">
              </div>
              <div class="form-group">
                <input name="message.title" class="form-control" placeholder="标题(30字以内):">
              </div>
              <div class="form-group">
                <textarea name="message.contents" rows="8" id="message_content" class="form-control" placeholder="内容(300字以内)">${(message.content?html)!}</textarea>
              </div>
            </div>
            <div class="box-footer">
              <div class="float-right">
                <button type="submit"  class="btn btn-primary" onclick="bg.form.submit('newMessageForm',null,null,null);return false;"><i class="fa fa-envelope-o"></i> 发送</button>
              </div>
              <button type="reset" class="btn btn-default"  onclick="history.back(-1)"><i class="fa fa-times"></i>不写了</button>
            </div>
          </div>
[/@]
<script>
  function validateMessage(){
    var form =document.newMessageForm
    if(form['recipient.code'].value.length==0){
       alert("接受人没有填写");
       return false;
    }
    if(form['message.title'].value.length==0){
       alert("标题没有填写");
       return false;
    }
    if(form['message.title'].value.length>30) {
       alert("标题不能超过30个字,已经"+form['message.title'].value.length+"字了");
       return false;
    }
    if(form['message.contents'].value.length==0){
       alert("内容没有填写");
       return false;
    }
    if(form['message.contents'].value.length>300) {
       alert("内容不能超过300个字,已经"+form['message.contents'].value.length+"字了");
       return false;
    }
    return true;
  }
</script>
[@b.foot/]
