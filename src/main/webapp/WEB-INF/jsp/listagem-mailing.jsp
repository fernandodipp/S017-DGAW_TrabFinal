<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	import="java.util.*,com.aula.negocio.*,com.aula.modelo.*,com.aula.dados.*"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>ADM - Listagem do Mailings</title>
</head>
<body>
<br>${MENSAGEM}<br>

<%
	MailingServico servico = (MailingServico)  application.getAttribute("lista_mailing");
	if(servico == null){
		servico = new MailingServico();
		application.setAttribute("lista_mailing", servico);
		//servico.salvar(request.getAttribute("nome").toString());
	}else{
		//servico.salvar(request.getAttribute("nome").toString());
	}
	
	ArrayList<MailingModelo> lista = servico.listar();

	out.println("<table border=1>");
	out.println("<th>");
	out.println("Id");
	out.println("</th>");
	out.println("<th>");
	out.println("Nome");
	out.println("</th>");
	out.println("<th>");
	out.println("Email");
	out.println("</th>");
	out.println("<th>");
	out.println("Telefone");
	out.println("</th>");
	out.println("<th>");
	out.println("Editar");
	out.println("</th>");
	out.println("<th>");
	out.println("Apagar");
	out.println("</th>");
	for (MailingModelo mailing : lista) {
		out.println("<tr>");
			out.println("<td>");
				out.println(mailing.id);
			out.println("</td>");
			out.println("<td>");
				out.println(mailing.nome);
			out.println("</td>");
			out.println("<td>");
				out.println(mailing.email);
			out.println("</td>");
			out.println("<td>");
				out.println(mailing.telefone);
			out.println("</td>");
			out.println("<td>");
				out.println("<a href='/atualizar/" + mailing.id);
												out.println("'>Editar</a>");
			out.println("</td>");
			out.println("<td>");
				out.println("<a href='/apagar/" + mailing.id);
												out.println("'>Apagar</a>");
			out.println("</td>");
		out.println("</tr>");
	}
	out.println("</table>");
	%>

</body>
</html>