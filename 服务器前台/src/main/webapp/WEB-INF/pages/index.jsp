<%@ page language="java" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head lang="en">
    <%@include file="file/cdn.jsp"%>
    <title>WINonline</title>
    <meta charset="UTF-8">
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <link href="<%=cdnDomain%>static/plugins/scroll/jquery.mCustomScrollbar.css" rel="stylesheet">
    <link changeable="true" href="<%=cdnDomain%>static/css/common-<%=currentTheme%>.css?v=${cdnVersion}" rel="stylesheet">
</head>
<body>
<!--*******************************左侧导航******************************-->
<div class="aside-wrapper" id="lottery-menu"></div>
<!--*******************************左侧导航******************************-->

<!--*******************************内容******************************-->
<div class="section-wrapper">
    <%
        String link = request.getParameter("link");
        String indexSrc;
        if (link == null || "".equals(link)) {
            indexSrc = "center";
        }
        else {
            indexSrc = link;
        }
    %>
    <iframe changeable="true" src="<%=indexSrc%>" frameborder="0" id="iframe" name="iframe" scrolling="0" width="100%" height="100%" class="bg-gray"></iframe>
</div>
<!--*******************************内容******************************-->

<div id="index_template_tpl" style="display: none;"></div>

<script src="<%=cdnDomain%>static/plugins/jquery/jquery.min.js"></script>
<script src="<%=cdnDomain%>static/plugins/jquery/jquery.cookie.js"></script>
<script src="<%=cdnDomain%>static/plugins/scroll/jquery.mCustomScrollbar.min.js"></script>
<script src="<%=cdnDomain%>static/plugins/sweet/core.js"></script>
<script src="<%=cdnDomain%>static/plugins/sweet/sweet.all.min.js"></script>
<script src="<%=cdnDomain%>static/plugins/core.js"></script>
<script src="<%=cdnDomain%>static/js/common.js?v=${cdnVersion}"></script>
<script src="<%=cdnDomain%>static/js/index.js?v=${cdnVersion}"></script>
</body>
</html>