<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="zh-CN">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script type="text/javascript" src="script/jquery.min.js"></script>
<script type="text/javascript" src="script/jcanvas.min.js"></script>
<link rel="stylesheet" type="text/css" href="style/style.css" />
<link rel="stylesheet" type="text/css" href="style/buildstyle.css" />
<script type="text/javascript">
$(document).ready(function(){
	$(".pointer").css({"right":"403px"});
	$(".pointer").show();
	$(".but_vector").css({"background":"#54B9C7"});
	$(".but_dna").css({"background":"#1F292D"});
	$(".but_pro").css({"background":"#1F292D"});
	$(".pro_right").hide();
	$(".pro_left").hide();
	$(".dna_left").hide();
	$(".dna_right").hide();
	$(".dna_build").hide();
	$(".build_left").hide();
	$(".build_right").hide();
	$(".vector_left").fadeIn(1000);
	$(".vector_right").fadeIn(1000);
	
  $(".but_vector").click(function(){
	$(".pointer").css({"right":"403px"});
	$(".pointer").show();
	$(".but_vector").css({"background":"#54B9C7"});
	$(".but_dna").css({"background":"#1F292D"});
	$(".but_pro").css({"background":"#1F292D"});
	$(".pro_right").hide();
	$(".pro_left").hide();
	$(".dna_left").hide();
	$(".dna_right").hide();
	$(".dna_build").hide();
	$(".build_left").hide();
	$(".build_right").hide();
	$(".vector_left").fadeIn(1000);
	$(".vector_right").fadeIn(1000);
  });
  
  $(".but_dna").click(function(){
	$(".pointer").css({"right":"288px"});
	$(".pointer").show();
	$(".but_dna").css({"background":"#54B9C7"});
	$(".but_vector").css({"background":"#1F292D"});
	$(".but_pro").css({"background":"#1F292D"});
	$(".vector_left").hide();
	$(".vector_right").hide();
	$(".pro_right").hide();
	$(".pro_left").hide();
	$(".build_left").hide();
	$(".build_right").hide();
	$(".dna_left").fadeIn(1000);
	$(".dna_right").fadeIn(1000);
	$(".dna_query").fadeIn(1000);
	$(".dna_build").fadeIn(1000);
	$(".dna_but_build").fadeIn(1000);
	$(".dna_but_cancel").hide();
	$(".dna_build_form").hide();
  });
  
  $(".but_pro").click(function(){
	$(".pointer").css({"right":"168px"});
	$(".but_pro").css({"background":"#54B9C7"});
	$(".but_dna").css({"background":"#1F292D"});
	$(".but_vector").css({"background":"#1F292D"});
	$(".dna_left").hide();
	$(".dna_right").hide();
	$(".dna_build").hide();
	$(".vector_left").hide();
	$(".vector_right").hide();
	$(".build_left").hide();
	$(".build_right").hide();
	$(".pro_left").fadeIn(1000);
	$(".pro_right").fadeIn(1000);
  });
    
  $(".but_upload").click(function(){
	$(".pointer").css({"right":"50px"});
	$(".but_pro").css({"background":"#1F292D"});
	$(".but_dna").css({"background":"#1F292D"});
	$(".but_vector").css({"background":"#1F292D"});
	$(".dna_left").hide();
	$(".dna_right").hide();
	$(".dna_build").hide();
	$(".vector_left").hide();
	$(".vector_right").hide();
	$(".pro_left").hide();
	$(".pro_right").hide();
	$(".build_left").fadeIn(1000);
	$(".build_right").fadeIn(1000);
  });
 
<!-- data_type的select项控制维度或者分段长度的显示 -->
var data_type_selection = {"protein":"build_frag_row","dna":"build_frag_row","vector":"build_dim_row"};
$("#data_type").bind("change",function(){
	var divId = data_type_selection[this.value];
	$("#build_dim_row").css({"display" : "none"}); 
	$("#build_frag_row").css({"display" : "none"}); 
	$("#"+divId).css({"display" : ""}); 
	if(this.value=="dna"){
		$("#build_frag").val(18);
		$("#build_dim").val(""); 
	}
	else if(this.value=="protein"){
		$("#build_frag").val(6);
		$("#build_dim").val("");
		}
	else{
		$("#build_frag").val("");
		$("#build_dim").val(2); 
	}	
});
<!-- 根据PSM的值决定Fft Scale栏的值以及是否显示 -->
$("#build_psm").bind("change",function(){
	if(this.value == "pca"){
		$("#build_fft_scale_row").css({"display":""});
	}
	else{
		$("#build_fft_scale_row").css({"display":"none"});
		$("#build_fft_scale").val(30);
	}
});
<!-- 根据dpm的值决定build_v的读写状态以及值 -->
$("#build_dpm").bind("change",function(){
	if(this.value=="cght" || this.value=="ght"){
		$("#build_v").val(2);
		$("#build_v")[0].readOnly = true;
	}
	else{
		$("#build_v")[0].readOnly = false;
	}
});

<!-- 快速建立模式当中数据类型控制默认值 -->
$("#fast_data_type").bind("change",function(){
if(this.value=="dna"){
	$("#build_frag").val(18);
	$("#build_dim").val(""); 
}
else if(this.value=="protein"){
	$("#build_frag").val(6);
	$("#build_dim").val("");
	}
else{
	$("#build_frag").val("");
	$("#build_dim").val(2); 
}	
});

<!-- 新建表格初始状态 -->
$("#build_dim_row").css({"display" : "none"});
$("#build_f_row").css({"display":"none"});
$("#build_dpm_row").css({"display":"none"});
$("#build_psm_row").css({"display":"none"});
$("#build_frag_row").css({"display":"none"});
$("#build_v_row").css({"display":"none"});
$("#build_m_row").css({"display":"none"});
$("#data_type_row").css({"display":"none"});
$("#build_fft_scale_row").css({"display":"none"});
$(".fast_options_btn").hide();
<!-- 新建时高级选项的触发事件函数 -->
$("#adv_options_btn").click(function(){
	$("#build_f_row").css({"display":""});
	$("#build_dpm_row").css({"display":""});
	$("#build_psm_row").css({"display":""});
	$("#build_v_row").css({"display":""});
	$("#build_m_row").css({"display":""});
	$("#data_type_row").css({"display":""});
	$("#fast_data_type_row").css({"display":"none"});
	$(".adv_options_btn").hide();
	$(".fast_options_btn").fadeIn(1000);
<!-- 恢复默认值 -->
	$("#build_f").val(3); 
	$("#build_dpm").val(""); 
	$("#build_psm").val(""); 
	$("#build_v").val(2); 
	$("#build_m").val(100); 
	$("#build_fft_scale").val(30);
<!-- 将fast_data_type的值传递给data_type,并通过fast_data_type的值判定dimension以及fragment的显示以及初始值 -->	
	var fast_data_type = document.getElementById("fast_data_type"); 
	$("#data_type").val(fast_data_type.options[fast_data_type.selectedIndex].value);
	if(fast_data_type.options[fast_data_type.selectedIndex].value=="dna"){
		$("#build_frag").val(18);
		$("#build_dim").val(""); 
		$("#build_dim_row").css({"display":"none"});
		$("#build_frag_row").css({"display":""});
	}
	else if(fast_data_type.options[fast_data_type.selectedIndex].value=="protein"){
		$("#build_frag").val(6);
		$("#build_dim").val("");
		$("#build_dim_row").css({"display":"none"});
		$("#build_frag_row").css({"display":""});
		}
	else if(fast_data_type.options[fast_data_type.selectedIndex].value=="vector"){
		$("#build_frag").val("");
		$("#build_dim").val(2); 
		$("#build_dim_row").css({"display":""});
		$("#build_frag_row").css({"display":"none"});
	}
});
<!-- 从高级选项返回成快速建立模式 -->
$("#fast_options_btn").click(function(){
	$("#build_dim_row").css({"display" : "none"});
	$("#build_f_row").css({"display":"none"});
	$("#build_dpm_row").css({"display":"none"});
	$("#build_psm_row").css({"display":"none"});
	$("#build_frag_row").css({"display":"none"});
	$("#build_v_row").css({"display":"none"});
	$("#build_m_row").css({"display":"none"});
	$("#data_type_row").css({"display":"none"});
	$("#fast_data_type_row").css({"display":""});
	$("#build_fft_scale_row").css({"display":"none"});
	
	$(".fast_options_btn").hide();
	$(".adv_options_btn").fadeIn(1000);
<!-- 恢复默认值 -->
	$("#build_frag").val(6); 
	$("#build_f").val(3); 
	$("#build_dpm").val(""); 
	$("#build_psm").val(""); 
	$("#build_v").val(2); 
	$("#build_m").val(100); 
	$("#build_dim").val(2);
	$("#build_fft_scale").val(30);
<!-- 将data_type的值传递给fast_data_type,并通过data_type的值判定dimension以及fragment的默认值 -->
	var data_type = document.getElementById("data_type"); 
	$("#fast_data_type").val(data_type.options[data_type.selectedIndex].value);	
	if(data_type.options[data_type.selectedIndex].value=="dna"){
		$("#build_frag").val(18);
		$("#build_dim").val(""); 
	}
	else if(data_type.options[data_type.selectedIndex].value=="protein"){
		$("#build_frag").val(6);
		$("#build_dim").val(""); 
		}
	else if(data_type.options[data_type.selectedIndex].value=="vector"){
		$("#build_frag").val("");
		$("#build_dim").val(2);  
	}
});

});
</script>
<title>基于非线性降维的通用搜索引擎(Demo)</title>
</head>
<body>
<div id="container">
	<div class="header">
		<div class="row">
		<div class="left">
			<div class="til">
			<h1>基于非线性降维的通用搜索引擎</h1>
			<!--<input type="button" class="but_build" value="上传数据" style="cursor:pointer;float: right;border-bottom-left-radius:3px;border-top-left-radius:3px;border-bottom-right-radius:3px;border-top-right-radius:3px;border:0px solid #000;vertical-align:top;width:130px;height:37px;font-size:22px;font-weight:600;background:#81B74D;color:white"> -->
			</div>
		</div>
		<div class="right">
			<div class="but_vector"><img src="./image/vector.png"  alt="dna" /></div>
			<div class="but_dna"><img src="./image/dna.png"  alt="dna" /></div>
			<div class="but_pro"><img src="./image/protein_but.png"  alt="dna" /></div>
			<div class="but_upload"><img src="./image/upload_but.png" alt="dna"></div>
			<div class="pointer">
			</div>
		</div>
		</div>
	</div>
	<div class="content">
		<div class="vector_left">
			<img src="./image/ivector.png" style="width:100%;height:100%;" alt="dna" />
		</div>
		<div class="vector_right">
			<div class="lab">
				<h1>向量搜索</h1>
				<h1>Vector</h1>
				<p>在这里进行向量的搜索..格式 <strong style="font-size:18px;color:#f87a7a">x,y,z,radian</strong> </p>
			</div>
			
			<form class="" id="" action="vector.action" style="position:relative;text-align:left;margin-left:50px">
				<input type="text" name="xyz" value="2,2,2,0.2" style="border-bottom-left-radius:3px;border-top-left-radius:3px;border:0px solid #000;padding-left:10px;width:400px;height:35px;font-size:16px;border:none;vertical-align:middle"/><input type="submit" value="搜索" style="cursor:pointer;border-bottom-right-radius:3px;border-top-right-radius:3px;border:0px solid #000;vertical-align:middle;width:130px;height:37px;font-size:22px;font-weight:600;background:#81B74D;color:white" />
			</form>
		</div>
		<div class="dna_left">
			<img src="./image/idna.jpg" style="width:100%;height:100%;" alt="dna" />
		</div>
		<div class="dna_right">
		<div class="dna_query">
			<div class="lab">
				<h1>DNA序列搜索</h1>
				<h1>DNA Sequence</h1>
				<p>在这里进行DNA的搜索..一些搜索格式介绍</p>
				<p> <strong  style="font-size:18px;color:#f87a7a">dna,radian</strong></p>
			</div>
			
			<form action="dna.action" style="position:relative;text-align:left;margin-left:50px">
				<input type="text" name="dnastr" value="TCGGATCGCCAT,5.0" style="border-bottom-left-radius:3px;border-top-left-radius:3px;border:0px solid #000;padding-left:10px;width:400px;height:35px;font-size:16px;border:none;vertical-align:middle" /><input type="submit" value="搜索" style="cursor:pointer;border-bottom-right-radius:3px;border-top-right-radius:3px;border:0px solid #000;vertical-align:middle;width:130px;height:37px;font-size:22px;font-weight:600;background:#81B74D;color:white" />
			</form>
		</div>
		
		</div>

		<div class="pro_left">
			<img src="./image/protein.png" style="width:100%;height:100%;" alt="dna" />
		</div>
		<div class="pro_right">
			<div class="lab">
				<h1>蛋白质搜索</h1>
				<h1>Protein</h1>
				<p>在这里进行蛋白质的搜索..一些搜索格式介绍</p>
				<p> <strong style="font-size:18px;color:#f87a7a">protein,radian</strong></p>
			</div>
			
			<form action="protein.action" style="position:relative;text-align:left;margin-left:50px">
				<input type="text" name="prostr" value="DGSY,10.0" style="border-bottom-left-radius:3px;border-top-left-radius:3px;border:0px solid #000;padding-left:10px;width:400px;height:35px;font-size:16px;border:none;vertical-align:middle" /><input type="submit" value="搜索" style="cursor:pointer;border-bottom-right-radius:3px;border-top-right-radius:3px;border:0px solid #000;vertical-align:middle;width:130px;height:37px;font-size:22px;font-weight:600;background:#81B74D;color:white" />
			</form>
		</div>
		
		<div class="build_left">
			<img src="./image/ibuildtree.png" style="width:100%;height:100%;" alt="dna" />
	  </div>
	  <div class="build_right">
		<div class="build_title">
			<h1>BUILD INDEX</h1>
		</div>
	  	<div class="build_form">
			<form action="" method="post">
				<table >
					<tr id="build_file_row"><td class="label">Data:  </td>
						<td class="dna_file_box">
							<input type='text' name='textfield' id='textfield' class='file_text' />
							<input type='button' name='textbutton' id='textbutton' value='Browse' class='file_button'/>
							<input type="file" name="fileField" id="fileField" class="file_file"  onchange="javascript:document.getElementById('textfield').value=this.value;"/></td></tr>
					<tr id="fast_data_type_row"><td class="label">Type:</td>
						<td >
							<select class="data_type" id="fast_data_type" onChange="change(this.value)">
								<option value="protein">Protein</option> 
								<option value="dna">DNA</option> 
								<option value="vector">Vector</option>
							</select></td></tr>
					<tr id="data_type_row"><td class="label">Type:</td>
						<td >
							<select class="data_type" id="data_type" onChange="change(this.value)">
								<option value="protein">Protein</option> 
								<option value="dna">DNA</option> 
								<option value="vector">Vector</option>
							</select></td></tr>
					<tr id="build_f_row"><td class="label">Fanout:</td>
						<td ><input type='text' name='build_f' id='build_f' class='build_f' value=3 /></td></tr>
					<tr id="build_frag_row" ><td class="label" id="build_frag_label">Fragment:</td>
						<td ><input type='text' name='build_frag' id='build_frag' class='build_frag' value=6 /></td></tr>
					<tr id="build_dim_row"><td class="label ">Dimension:</td>
						<td ><input type='text' name='build_dim' id='build_dim' class='build_dim' /></td></tr>
					<tr id="build_dpm_row"><td class="label">Partition Method:</td>
						<td >
							<select class="build_dpm" id="build_dpm"> 
								<option value="mvpt" selected=true >MVPT</option> 
								<option value="cght" >CGHT</option> 
								<option value="ght" >GHT</option>
							</select></td></tr>
					<tr id="build_v_row"><td class="label">Number of Pivots:</td>
						<td ><input type='text' name='build_v' id='build_v' class='build_v' value=2 /></td></tr>
					<tr id="build_m_row"><td class="label ">Maximum Leaf Children:</td>
						<td ><input type='text' name='build_m' id='build_m' class='build_m' value=100 /></td></tr>
					<tr id="build_psm_row"><td class="label">Pivot Selection Method:</td>
						<td >
							<select class="build_psm" id="build_psm"> 
								<option value="fft" selected=true >FFT</option> 
								<option value="pca">PCA</option> 
								<option value="lle">LLE</option>
								<option value="cov">COV</option>
								<option value="cor">COR</option>
							</select></td></tr>
					<tr id="build_fft_scale_row"><td class="label">FFT SCALE:</td>
						<td><input type="text" name="build_fft_scale" id="build_fft_scale" class="build_fft_scale"  value=30 /></td></tr>
					<tr ><td></td>
					<td><a onclick="" id="adv_options_btn" class="adv_options_btn" style="display:block" >+ Advanced</a>
							 <a onclick="" id="fast_options_btn" class="fast_options_btn" style="display:block" >- Easy Build</a>
							 <button type="submit" class="form_submit">submit</button></td></tr>
				</table>
			</form>
		</div>
	  </div>
	</div>
</div>
</body>
</html>
