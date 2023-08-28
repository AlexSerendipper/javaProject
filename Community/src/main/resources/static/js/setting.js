$(function (){
    // 表示当点击提交表单，触发表单提交事件时，事件由upload函数处理
    $("#uploadForm").submit(upload);
});


function upload(){
    $.ajax({
        url: "http://upload-z2.qiniup.com",  // 这是在七牛云官网的存储空间copy过来的上传地址
        data: new FormData($("#uploadForm")[0]),  // 上传文件ajax请求要求1：
        processData:false,  // 上传文件ajax请求要求2：阻止表单自动将表单中的内容转换为字符串提交给服务器
        contentType:false,  // 上传文件ajax请求要求3：设置上传的文件的类型，这里就是不让jquery设置上传的类型
        method:"POST",
        // 服务器返回的数据就是d，不用像原生做法那也去做转换了
        success:function (d) {
            if(d && d.code==0){
                // 更新用户头像访问路径
                $.ajax({
                    url: DOMAIN + CONTEXT_PATH + "/user/header/url",
                    data: {"fileName":$("input[name='key']").val()},
                    type:"POST",
                    // 服务器返回的数据就是d，不用像原生做法那也去做转换了
                    success:function (d) {
                        if(d.code==0){
                            location.reload();
                        }else {
                            alert(d.msg);
                        }
                    },
                    // 设置了返回数据类型，根据返回数据类型自动转换为js对象！！就不用像原生做法一样去转换了（json.parse()）
                    dataType : "json"
                });
            }else {
                alert("上传失败！")
            }
        },
        // 这里七牛云返回的就是json对象格式，而非json字符串，不知道需不需要设置dataType
        // dataType : "json"
    });

    // 注意此处必须返回false，否则form仍会试图提交表单
    return false;
}