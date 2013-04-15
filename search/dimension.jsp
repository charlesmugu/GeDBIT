<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link rel="stylesheet" type="text/css" href="style/global.css" />
		<link rel="stylesheet" type="text/css" href="style/dimsearch.css" />

		<script type="text/javascript" src="script/jquery.min.js"></script>
		<script type="text/javascript" src="script/jcanvas.min.js"></script>
		<title>搜索结果-向量</title>
	</head>

<body>
<div class="header">
	<div class="container_w">
		<div class="logol">
			<img src="./image/img.png" alt="image_search"/>
		</div>
		
		<div class="return">
			<a href="index.jsp">
				<div class="but_out"><img style="border:0" src="image/gohome.png" alt="" /></div>
			</a>
		</div>
	</div>
</div>

<div id="dim_search_summary">
	<div class="container_w">
		<div id="dim_search_summary_details">
			<div id="dim_search_summary_details_word">
				<h2>
					<span> <s:property value="resnum" /></span>
					Results
				</h2>
				<p class="search_stats">
					Searched over <s:property value="allcount" /> points in <s:property value="time" /> seconds.
				</p>
				<p class="search_source">for point <strong><s:property value="xyz" /></strong></p>
			</div>
		</div>
	</div>
</div>

<div id="dim_search_results">
	<div class="container_w">
		<div class="dim_search_results_list">
			<div class="dim_search_results_item clearfix">
				<div class="dim_search_results_item_details">
					<span>Results:</span>
					<p>
						<strong> <s:property value="resnum" /> </strong> points <br/>
					</p>
					<div style="height:500px;overflow:auto">
						<s:iterator value="rescoord" id="num" status="st">
						<p>
						<s:property value="key"/> point:<br/><div class="resu"><s:property value="value" /></div><br/>
						</p>
						</s:iterator>
					</div>
				</div>
				<div id="tips">
					Tip:</br>
					1.点击鼠标旋转/停止坐标系</br></br>
					2.在停止旋转时，鼠标移动至点上可显示具体坐标</br>
				</div>
				<div class="dim_canvas">
<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="500" height="500" id="FlashID" title="Flash">
  <param name="movie" value="image/3dPlayer.swf" />
  <param name="quality" value="high" />
  <param name="base" value="."/>
  <param name="wmode" value="opaque" />
  <param name="swfversion" value="15.0.0.0" />
  <!-- This param tag prompts users with Flash Player 6.0 r65 and higher to download the latest version of Flash Player. Delete it if you don’t want users to see the prompt. -->
  <param name="expressinstall" value="Scripts/expressInstall.swf" />
  <!-- Next object tag is for non-IE browsers. So hide it from IE using IECC. -->
  <!--[if !IE]>-->
  <object type="application/x-shockwave-flash" data="image/3dPlayer.swf" width="500" height="500">
    <!--<![endif]-->
    <param name="quality" value="high" />
    <param name="wmode" value="opaque" />
    <param name="swfversion" value="15.0.0.0" />
    <param name="expressinstall" value="Scripts/expressInstall.swf" />
    <!-- The browser displays the following alternative content for users with Flash Player 6.0 and older. -->
    <div>
      <h4>Content on this page requires a newer version of Adobe Flash Player.</h4>
      <p><a href="http://www.adobe.com/go/getflashplayer"><img src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Get Adobe Flash player" width="112" height="33" /></a></p>
    </div>
    <!--[if !IE]>-->
  </object>
  <!--<![endif]-->
</object>
				</div>
				<script type="text/javascript" src="script/xyz.js"></script>
			</div>
		</div>
	</div>
</div>

<div class="footer">

</div>
</body>


</html>