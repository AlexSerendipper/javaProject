$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	var entityId = $(btn).prev().val();  // 前一个节点
	// btn按钮如果有该样式（一个蓝色的样式，有该样式代表用户可以关注
	if($(btn).hasClass("btn-info")) {
		$.ajax({
			url: DOMAIN + CONTEXT_PATH + "/follow",
			data: {"entityType":3,"entityId":entityId},
			type:"POST",
			// 服务器返回的数据就是d，不用像原生做法那也去做转换了
			success:function (d) {
				if(d.code==0){
					// 关注TA
					// $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
					window.location.reload()
				}else {
					alert(d.msg);
				}
			},
			// 设置了返回数据类型，根据返回数据类型自动转换为js对象！！就不用像原生做法一样去转换了（json.parse()）
			dataType : "json"
		});
	} else {
		$.ajax({
			url: DOMAIN + CONTEXT_PATH + "/unfollow",
			data: {"entityType":3,"entityId":entityId},
			type:"POST",
			// 服务器返回的数据就是d，不用像原生做法那也去做转换了
			success:function (d) {
				if(d.code==0){
					// 取消关注
					// $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
					window.location.reload()
				}else {
					alert(d.msg);
				}
			},
			// 设置了返回数据类型，根据返回数据类型自动转换为js对象！！就不用像原生做法一样去转换了（json.parse()）
			dataType : "json"
		});
	}

}