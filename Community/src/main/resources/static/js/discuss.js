
$(function (){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
})

// 点赞功能
function like(element,entityType,entityId,targetUserId,postId){
    $.ajax({
        url: DOMAIN + CONTEXT_PATH + "/like",
        data: {"entityType":entityType,"entityId":entityId,"targetUserId":targetUserId,"postId":postId},
        type:"POST",
        // 服务器返回的数据就是d，不用像原生做法那也去做转换了
        success:function (d) {
            if(d.code == 0){
                $(element).children("i").text(d.likeCount);
                $(element).children("b").text(d.likeStatus==1?"已赞":"赞");
            }else {
                alert(d.msg)
            }
        },
        // 设置了返回数据类型，根据返回数据类型自动转换为js对象！！就不用像原生做法一样去转换了（json.parse()）
        dataType : "json"
    });
}

// 置顶功能
function setTop(){
    var postId = $("#postId").val();
    $.ajax({
        url: DOMAIN + CONTEXT_PATH + "/discuss/top",
        data: {"id":postId},
        type:"POST",
        // 服务器返回的数据就是d，不用像原生做法那也去做转换了
        success:function (d) {
            if(d.code == 0){
                // 点过置顶按钮后，就要把该按钮设置为disabled
                $("#topBtn").attr("disabled","disabled");
            }else {
                alert(d.msg)
            }
        },
        // 设置了返回数据类型，根据返回数据类型自动转换为js对象！！就不用像原生做法一样去转换了（json.parse()）
        dataType : "json"
    });
}

// 加精功能
function setWonderful(){
    var postId = $("#postId").val();
    $.ajax({
        url: DOMAIN + CONTEXT_PATH + "/discuss/wonderful",
        data: {"id":postId},
        type:"POST",
        // 服务器返回的数据就是d，不用像原生做法那也去做转换了
        success:function (d) {
            if(d.code == 0){
                // 点过置顶按钮后，就要把该按钮设置为disabled
                $("#wonderfulBtn").attr("disabled","disabled");
            }else {
                alert(d.msg)
            }
        },
        // 设置了返回数据类型，根据返回数据类型自动转换为js对象！！就不用像原生做法一样去转换了（json.parse()）
        dataType : "json"
    });
}

// 删除功能
function setDelete(){
    var postId = $("#postId").val();
    $.ajax({
        url: DOMAIN + CONTEXT_PATH + "/discuss/delete",
        data: {"id":postId},
        type:"POST",
        // 服务器返回的数据就是d，不用像原生做法那也去做转换了
        success:function (d) {
            if(d.code == 0){
                // 帖子删除后直接跳转到首页
                location.href = DOMAIN + CONTEXT_PATH + "/index";
            }else {
                alert(d.msg)
            }
        },
        // 设置了返回数据类型，根据返回数据类型自动转换为js对象！！就不用像原生做法一样去转换了（json.parse()）
        dataType : "json"
    });
}