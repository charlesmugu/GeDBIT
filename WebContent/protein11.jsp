<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link rel="stylesheet" type="text/css" href="style/global.css" />
		<link rel="stylesheet" type="text/css" href="style/picsearch.css" />		
		<script type="text/javascript" src="script/jquery.min.js"></script>
		<script type="text/javascript" src="script/content_zoom.js"></script>
		<script type="text/javascript">
		$(document).ready(function() {
			$('div.img_thumbs a').fancyZoom({scaleImg: true, closeOnClick: true});
			$('div.img_thumbs p a').fancyZoom({scaleImg: true, closeOnClick: true});
		});
		</script>
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

<div id="pic_search_summary">
	<div class="container_w">

		<div id="pic_search_summary_details">
			<div id="pic_search_summary_details_img">
				<div class="img_thumbs">
					<a href="#pic_0"><img alt="Query Image" src="./image/picsearch/imgsrc/<s:property value="usrimg" />"></a>
					<p class="pic_search_img_status"><s:property value="usrimg" /></p>
				</div>
			</div>
			<div id="pic_search_summary_details_word">
				<h2>
					<span> <s:property value="mapsize" /></span>
					Results
				</h2>
				<p class="search_stats">
					Searched over 1234 images in xxx seconds.
				</p>
				<p class="search_source">for file: <s:property value="usrimg" /></p>
			</div>
		</div>
	</div>
</div>

<div id="pic_search_results">
	<div class="container_w">
		<div class="pic_search_results_list">
		
		
		<s:iterator value="simimg" status="st">
			<div class="pic_search_results_item clearfix">
				<div class="pic_search_results_item_image">
					<div class="img_thumbs">
						<a href="#pic_<s:property value='key'/>"><img  alt="Result image" src="./image/picsearch/imgres/<s:property value="value" />"></a>
						<p><a href="#pic_0">Compare</a>
					</div>
				</div>
				<div class="pic_search_results_item_details">
					<span>
						<strong><s:property value="value" /></strong>
						<br>
						JPEG image
						<br>
						500x334, 134 KB
					</span>
				</div>
			</div>
</s:iterator>
			<div class="pic_search_results_updown clearfix"></div>
		</div>
	</div>
</div>

<div class="footer">

</div>
<!--图片放大-->
<div class="zoomshow" id="pic_0" style="display:none;"><img src="./image/picsearch/imgsrc/<s:property value="usrimg" />" /></div>
<s:iterator value="simimg" status="st">
<div class="zoomshow" id="pic_<s:property value="key"/>" style="display:none;"><img src="image/picsearch/imgres/<s:property value="value"/>" /></div>
</s:iterator>


</body>


</html>