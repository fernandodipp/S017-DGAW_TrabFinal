<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Cadastra Mailing</title>
</head>
<body>
<p> Cadastro de Mailing </p>
<form action="mailing" method="post">
	Nome: <input type="text" name="nome"/> <br>
	Email: <input type="text" name="email"/> <br>	
	Telefone com DDD: <input type="text" name="telefone"/> <br>
	<input type="submit" value="Cadastrar"/>
</form>
</body>
</html>