<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<script type="text/javascript" src="script/jquery.min.js"></script>
		<link rel="stylesheet" type="text/css" href="style/global.css" />
		<link rel="stylesheet" type="text/css" href="style/dnasearch.css" />
		<script type="text/javascript">
		$(document).ready(function(){
			
});
</script>
<title>检索索引结果</title>
	</head>

<body>
<div class="header">
	<div class="container_w">
		<div class="logol">
			<img src="./image/dna.png" alt="image_search"/>
		</div>
		<div class="return">
			<a href="index.jsp">
				<div class="but_out"><img style="border:0" src="image/gohome.png" alt="" /></div>
			</a>
		</div>
	</div>
</div>

<div id="result_message">
	<div class="container_w" style="text-align:center;">
        <img src="./image/ibuildtree.png" style="width:300px;height:300px;margin:auto;" alt="dna" />
        <h2>Sussess!</h2>
        <a href="index.jsp">go to search page --></a>
        <!--
		<div id="dna_search_summary_details">
			<div id="dna_search_summary_details_word">
				<h2>
					<span><strong><s:property value='pronum'/></span>
					Results
				</h2>
				<p class="search_stats">
					Searched over <s:property value="allcount" /> Protein in <s:property value="time" /> seconds.
				</p>
				<p class="search_source">for Protein: <strong><s:property value='prostr'/></strong></p>
			</div>
		</div>-->
	</div>
</div>


</body>


</html>