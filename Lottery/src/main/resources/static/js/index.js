// 点击发布后 发布帖子
$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	// // 在发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
	// var token = $("meta[name='_csrf']").attr("content");
	// var header = $("meta[name='_csrf_header']").attr("content");
	// $(document).ajaxSend(function (e,xhr,options){
	// 	xhr.setRequestHeader(header,token);
	// })


	// 点击发布后隐藏发布框
	$("#publishModal").modal("hide");
	// 发异步请求
	var title = $("#recipient-name").val()
	var content = $("#message-text").val()
	$.ajax({
		url: DOMAIN + CONTEXT_PATH + "/discuss/add",
		data: {"title":title,"content":content},
		type:"POST",
		// 服务器返回的数据就是d，不用像原生做法那也去做转换了
		success:function (d) {
			// 在提示框中返回提示的消息
			$("#hintBody").text(d.msg);
			// 显示提示框，显示后2s后自动隐藏
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 如果添加成功了，刷新页面（以看到自己发的帖子）
				if(d.code == 0){
					window.location.reload();
				}
			}, 2000);
		},
		// 设置了返回数据类型，根据返回数据类型自动转换为js对象！！就不用像原生做法一样去转换了（json.parse()）
		dataType : "json"
	});
}


$(".wrap").on("click", function () {
	$.ajax({
		url: DOMAIN + CONTEXT_PATH + "/getRaffleRecord",
		type:"GET",
		// 服务器返回的数据就是d，不用像原生做法那也去做转换了
		success:function (d) {
			let prizeRecordsString = "";
			for (var i = 0; i < d.length; i++) {
				let prize = d[i];
				let rank="";
				switch (prize.prizeId) {
					case 1:
						rank="一等奖(兴业offer)"
						break;
					case 2:
						rank="二等奖(法拉利)"
						break;
					case 3:
						rank="三等奖(索尼相机)"
						break;
					case 4:
						rank="四等奖(蓝牙耳机)"
						break;
					case 5:
						rank="五等奖(兴业公仔)"
						break;
					case 6:
						rank="参与奖(积分100)"
						break;
					case 7:
						rank="参与奖(积分50)"
						break;
					case 8:
						rank="参与奖(积分10)"
						break;
				}
				var prizetime = new Date(prize.prizeTime);
				var formattedPrizeTime = prizetime.toLocaleString();
				prizeRecordsString += "中奖时间: " + formattedPrizeTime + ", 奖品: " + rank +  "<br>";
			}
			// Create a new HTML element to display the records
			var recordsElement = document.createElement("div");
			recordsElement.innerHTML = prizeRecordsString;

			// Use a library like SweetAlert or native JavaScript alert to display the records in a popup
			// For example, using SweetAlert:
			Swal.fire({
				title: "Prize Records",
				html: recordsElement,
			});
		},
		// 设置了返回数据类型，根据返回数据类型自动转换为js对象！！就不用像原生做法一样去转换了（json.parse()）
		dataType : "json"
	});


})