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
<script type="text/javascript">
$(document).ready(function(){
  $(".but_vector").click(function(){
	$(".pointer").css({"right":"288px"});
	$(".but_vector").css({"background":"#54B9C7"});
	$(".but_dna").css({"background":"#1F292D"});
	$(".but_pro").css({"background":"#1F292D"});
	$(".pro_right").hide();
	$(".pro_left").hide();
	$(".dna_left").hide();
	$(".dna_right").hide();
	$(".vector_left").fadeIn(1000);
	$(".vector_right").fadeIn(1000);
  });
  
  $(".but_dna").click(function(){
	$(".pointer").css({"right":"168px"});
	$(".but_dna").css({"background":"#54B9C7"});
	$(".but_vector").css({"background":"#1F292D"});
	$(".but_pro").css({"background":"#1F292D"});
	$(".vector_left").hide();
	$(".vector_right").hide();
	$(".pro_right").hide();
	$(".pro_left").hide();
	$(".dna_left").fadeIn(1000);
	$(".dna_right").fadeIn(1000);
  });
  
  $(".but_pro").click(function(){
	$(".pointer").css({"right":"50px"});
	$(".but_pro").css({"background":"#54B9C7"});
	$(".but_dna").css({"background":"#1F292D"});
	$(".but_vector").css({"background":"#1F292D"});
	$(".dna_left").hide();
	$(".dna_right").hide();
	$(".vector_left").hide();
	$(".vector_right").hide();
	$(".pro_left").fadeIn(1000);
	$(".pro_right").fadeIn(1000);
  });
});
</script>
<title>并行分布式度量空间索引搜索引擎</title>
</head>
<body>
<div id="container">
	<div class="header">
		<div class="row">
		<div class="left">
			<div class="til">
			<h1>并行分布式度量空间索引搜索引擎</h1>
			</div>
		</div>
		<div class="right">
			<div class="but_vector"><img src="./image/vector.png"  alt="dna" /></div>
			<div class="but_dna"><img src="./image/dna.png"  alt="dna" /></div>
			<div class="but_pro"><img src="./image/protein_but.png"  alt="dna" /></div>
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
	</div>
</div>
</body>
</html>
