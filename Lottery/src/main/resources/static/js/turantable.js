//獎品項目
var prize_list = [
  {
    name: "兴业offer",
      rank:"一等奖",
    img: "https://img.icons8.com/?size=2x&id=44025&format=png"
  },
  {
    name: "法拉利",
      rank:"二等奖",
      img: "https://img.icons8.com/?size=2x&id=36720&format=png"
  },
  {
    name: "索尼相机",
      rank:"三等奖",
      img: "https://img.icons8.com/?size=2x&id=nevTPff0KmQx&format=png"
  },
  {
    name: "蓝牙耳机",
      rank:"四等奖",
      img: "https://img.icons8.com/?size=2x&id=cAfxAGkjSrQo&format=png"
  },
  {
    name: "兴业公仔",
      rank:"五等奖",
      img: "https://img.icons8.com/?size=2x&id=iT7sQs8KEOAp&format=png"
  },
    {
        name: "积分100",
        rank:"参与奖",
        img: "https://img.icons8.com/?size=2x&id=118292&format=png"
    },
  {
    name: "积分50",
      rank:"参与奖",
      img: "https://img.icons8.com/?size=2x&id=Bz1KNLgkXDQO&format=png"
  },
    {
        name: "积分10",
        rank:"参与奖",
        img: "https://img.icons8.com/?size=2x&id=BYrLfXd7xnkT&format=png"
    }
]

for(var i=0; i<=7; i++){
  $(".list ul").append("<li><p>"+prize_list[i].name+"</p><img src='"+prize_list[i].img+"'></li>");
}

// 获取数据并更新页面内容的函数
function fetchDataAndUpdate() {
    $.ajax({
        url: DOMAIN + CONTEXT_PATH + "/getScoreAndTimes",
        method: 'GET',
        dataType: 'json',
        success: function(data) {
            // 更新HTML元素的内容
            $('#points').text(data.score);
            $('#lotteryCount').text(data.lotteryTimes);
        },
        error: function(error) {
            console.error('获取数据时出错：', error);
        }
    });
}

// 在页面加载时获取数据并更新内容
$(document).ready(fetchDataAndUpdate);

// 假設iEnd是請求獲得的獎品結果
var iEnd = -1;
var iEndRotate=0;
var msg="";
$(".turntable_btn").on("click", function(){

    var $this = $(this);
    $.ajax({
      url: DOMAIN + CONTEXT_PATH + "/lottery",
      type:"POST",
      // 服务器返回的数据就是d，不用像原生做法那也去做转换了
      success:function (d) {
            // iEnd = Math.floor(Math.random() * 8);
            iEnd = d.iEnd;
            // console.log(iEnd);
          if(iEnd==-1) {
              alert('今日抽奖次数已用尽');
              return;
          }
          if(iEnd==-2) {
              alert('积分不足');
              return;
          }
          var prize = $(".list").find("li").eq(iEnd-1).find("p").html();
            // console.log(prize);
          var rank=prize_list[iEnd-1].rank
          
            rotating();
            //禁用
            $this.attr("disabled", "disabled");

            getgpt(prize_list[iEnd-1].name);

            setTimeout(function(){
                // 恢復按鈕
                $this.removeAttr("disabled");
                $(".list ul").removeClass("go");
                $(".polyline").removeClass("go");
                $(".circle circle").removeClass("go");
                var prize = $(".list").find("li").eq(iEnd-1).find("p").html().replace("<br>","");
                // console.log(prize);
                alert('恭喜獲得：'+ rank+prize + '!! '+msg);
                fetchDataAndUpdate();
            }, 4200);
      },
      // 设置了返回数据类型，根据返回数据类型自动转换为js对象！！就不用像原生做法一样去转换了（json.parse()）
      dataType : "json"
  });

    

});

function getgpt(prize) {
    $.ajax({
        url: "http://18.144.49.204:8060/v1/chat/completions",
        type: "POST",
        headers: {
            // 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3',
            'Content-Type': 'application/json',
            'Authorization': 'Bearer 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'
        },
        data: JSON.stringify({
            "model": "gpt-3.5-turbo",
            "messages": [{"role": "user", "content": `我参加了兴业银行的转盘抽奖活动，我抽中了${prize}，请你给我一段祝福语，字数不要太长`}],
            "temperature": 0.7
        }),
        // 服务器返回的数据就是d，不用像原生做法那也去做转换了
        success: function (d) {
            msg = d.choices[0].message.content;
        },
        // 设置了返回数据类型，根据返回数据类型自动转换为js对象！！就不用像原生做法一样去转换了（json.parse()）
        dataType: "json"
    });
}

function rotating()
{
  iEndRotate=(iEnd-1)*45;

    const gokeyframes = `
      @keyframes go {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(${1800 + iEndRotate}deg); }
  }
`;
    const styleElement = document.createElement('style');
    styleElement.textContent = gokeyframes;
    console.log(document.getElementsByTagName('head')[0]);
    console.log("132");
    document.getElementsByTagName('head')[0].appendChild(styleElement);
    // document.head.appendChild(styleElement);

      
  
        // console.log(iEnd);
    $(".list ul").addClass("go");
    $(".polyline").addClass("go");
    $(".circle circle").addClass("go");
    
    // var rotate = prize_list.attr("style");
    // var rotate_split_1 = rotate.split(":");
    // var rotate_split_2 = rotate_split_1[1].split("(");
    // var rotate_split_3 = rotate_split_2[1].split("deg");
    // //旋轉度
    // var rotate_deg = rotate_split_3[0];
    // // console.log(rotate_deg);

    switch (iEnd)
      {
      case 1:
        $(".polyline").css("transform","rotate(0deg)");
        $(".list ul").css("transform","rotate(0deg)");
        break;
      case 2:
        $(".polyline").css("transform","rotate(45deg)");
        $(".list ul").css("transform","rotate(45deg)");
        break;
      case 3:
        $(".polyline").css("transform","rotate(90deg)");
        $(".list ul").css("transform","rotate(90deg)");
        break;
      case 4:
        $(".polyline").css("transform","rotate(135deg)");
        $(".list ul").css("transform","rotate(135deg)");
        break;
      case 5:
        $(".polyline").css("transform","rotate(180deg)");
        $(".list ul").css("transform","rotate(180deg)");
        break;
      case 6:
        $(".polyline").css("transform","rotate(225deg)");
        $(".list ul").css("transform","rotate(225deg)");
        break;
      case 7:
        $(".polyline").css("transform","rotate(270deg)");
        $(".list ul").css("transform","rotate(270deg)");
        break;
      case 8:
        $(".polyline").css("transform","rotate(315deg)");
        $(".list ul").css("transform","rotate(315deg)");
        break;
      }

      
}
