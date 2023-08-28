$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

// 点击弹出框的发送按钮时，隐藏弹出框
function send_letter() {
	$("#sendModal").modal("hide");

	// 使用ajax异步发送请求
	var targetUserName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.ajax({
		url: DOMAIN + CONTEXT_PATH + "/message/send",
		data: {"targetUserName":targetUserName,"content":content},
		type:"POST",
		// 服务器返回的数据就是d，不用像原生做法那也去做转换了
		success:function (d) {
			if (d.code==0){
				$("#hintBody").text("发送成功");
			}else {
				$("#hintBody").text("服务器异常！");
			}
			// 无论成功或者失败，显示提示框，重载当前页面，提示框两秒后关闭
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		},
		// 设置了返回数据类型，根据返回数据类型自动转换为js对象！！就不用像原生做法一样去转换了（json.parse()）
		dataType : "json"
	});


}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}