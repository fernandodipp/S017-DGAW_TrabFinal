<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Nome inv�lido</title>
</head>
<body>
Erro no campo nome<br>
Preencha o campo nome corretamente<br>

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