<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>E-mail inválido</title>
</head>
<body>
Erro no campo e-mail <br>
Preencha o campo e-mail corretamente<br>

${MENSAGEM}
<br><button onclick="goBack()">Go Back</button>
<br>
<script>
function goBack() {
  window.history.back();
}
</script>
</body>
</html>