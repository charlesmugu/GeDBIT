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
			var soudna=$("#usrsub").val();
			for(var i=0;i<soudna.length;i++)
			{
			switch(soudna[i])
			{
					case 'A':
						$("#sources").append("<li><img src='./image/dna_a.png' alt='A'/></li>")
						break
					case 'T':
						$("#sources").append("<li><img src='./image/dna_t.png' alt='T'/></li>")
						break
					case 'G':
						$("#sources").append("<li><img src='./image/dna_g.png' alt='G'/></li>")
						break
					case 'C':
						$("#sources").append("<li><img src='./image/dna_c.png' alt='C'/></li>")
					break
			}
			}
			$("#sources").append("<li style='padding-right:40px'></li>");
			
			var size=$('.jc').size();
			for(var j=0;j<size;j++)
			{
			var result=$("#jscount"+j).val();
			for(var i=0;i<result.length;i++)
			{
			switch(result[i])
			{
					case 'A':
						$("#result"+j).append("<li><img src='./image/dna_a.png' alt='A'/></li>")
						break
					case 'T':
						$("#result"+j).append("<li><img src='./image/dna_t.png' alt='T'/></li>")
						break
					case 'G':
						$("#result"+j).append("<li><img src='./image/dna_g.png' alt='G'/></li>")
						break
					case 'C':
						$("#result"+j).append("<li><img src='./image/dna_c.png' alt='C'/></li>")
					break
			}
			}
			$("#result"+j).append("<li style='padding-right:40px'></li>");
			}

});
</script>
<title>搜索结果-DNA</title>
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

  

<div id="dna_search_summary">
	<div class="container_w">

		<div id="dna_search_summary_details">
			<div id="dna_search_summary_details_word">
				<h2>
					<span><strong><s:property value='dnanum'/></span>
					Results
				</h2>
				<p class="search_stats">
					Searched over <s:property value="allcount" /> DNAs in <s:property value="time" /> seconds.
				</p>
				<p class="search_source">for DNA: <strong><s:property value='dnastr'/></strong></p>
			</div>
			
			<div id="dna_search_summary_details_img">
				<ul class="dna_img_item" id="sources">
					 <input type="hidden" value="<s:property value="dnastr" />" id="usrsub"/>
				</div>
			</div>
		</div>
	</div>
</div>

<div id="dna_search_results">
	<div class="container_w">
		<div class="dna_search_results_list">

	<s:iterator value="result" id="num" status="st">
			<input type="hidden" value="<s:property value="value" />" id="jscount<s:property value='#st.index'/>" class="jc"/>
			<div class="dna_search_results_item clearfix">
				<div class="dna_search_results_item_details">
					From DNA: <a href="#"> <s:property value="key" /></a><br/>
					Similar part:<strong><s:property value="value" /></strong>
				</div>
				
				<div class="dna_search_results_item_img">
					<ul class="dna_img_item" id="result<s:property value='#st.index'/>">
				</div>
			</div>
	</s:iterator>
	
			<div class="dna_search_results_updown clearfix">
				<s:if test="%{page>0}">
					<a href="?dnastr=<s:property value='dnastr'/>&page=<s:property value='%{page-1}'/>" id= >上一页</a>
				</s:if>
				<s:if test="%{page>0}">
					<s:if test="%{page<allpage}">
						<span>|</span>
					</s:if>
				</s:if>
				<s:if test="%{page<allpage}">
					<a href="?dnastr=<s:property value='dnastr'/>&page=<s:property value='%{page+1}'/>" id= >下一页</a>	
				</s:if>
			</div>
		</div>
	</div>
</div>

<div class="footer">

</div>

</body>


</html>