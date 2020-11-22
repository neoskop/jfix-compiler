<%@page extends="jfix.compiler.TestTemplate" contentType="text/plain; charset=UTF-8"%>
<%@page import="java.util.*" %>
<%!
	String body;

	public String render(String body) {
		this.body = body;
		return toString();
	}
%>
<% for (int i=1;i<=3;i++) { %>Hello <%=body %>-<%=i%>. <% } %>