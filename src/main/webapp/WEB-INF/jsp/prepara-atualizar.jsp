<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Alterando dados</title>
</head>
<body>
<p> --ÁREA ADMIN-- </p>
<p> Atualização de Mailing </p>
<form action="/submitUpdate" method="post">
	ID: <input type="text" name="ID" value='${ID}' readonly/><br>
	Nome: <input type="text" name="nome"/> <br>
	Email: <input type="text" name="email"/> <br>	
	Telefone com DDD: <input type="text" name="telefone"/> <br>
	<input type="submit" value="Cadastrar"/>
	
</form>

<br><button onclick="goBack()">Go Back</button>
<br>
<script>
function goBack() {
  window.history.back();
}
</script>
</body>
</html>