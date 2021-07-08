package com.aula.negocio;

import java.util.ArrayList;
import com.aula.controller.*;
import com.aula.dados.*;
import com.aula.modelo.*;
import com.aula.negocio.*;
import com.aula.dados.MailingRepositorio;
import com.aula.modelo.MailingModelo;

public class MailingServico {
	
	MailingRepositorio repositorio = new MailingRepositorio();

	public boolean adminLog (String user, String password)
	{
		String userValido = "admin";
		String passValido = "admin";
		if (user.equals(userValido)) 
		{
			System.out.println(user);
			if (password.equals(passValido))
			{
				System.out.println(password);
				return true;
			}
		}return false;
	}
	
	public String salvar(MailingModelo mailing) {
		System.out.println("--EXECUTANDO SERVIÇO - SALVAR--");
		if (mailing.nome.equals("")) {
			System.out.println("--Adição de mailing reprovada, nome inválido--");
			return "--Adição de mailing reprovada, nome inválido--";
		} else if (mailing.email.equals("")) {
			System.out.println("--Adição de mailing reprovada, email inválido--");
			return "--Adição de mailing reprovada, email inválido--";
		} else if (mailing.telefone.equals("")) {
			System.out.println("--Adição de mailing reprovada, telefone inválido--");
			return "--Adição de mailing reprovada, telefone inválido--";
		} else {
		System.out.println("--Adição de mailing aprovada--");		
		repositorio.salvar(mailing);
		return "--Adição de mailing aprovada--";
	}}

	public ArrayList<MailingModelo> listar() {
		System.out.println("--EXECUTANDO SERVIÇO - LISTAR --");		
		return repositorio.listagem();
	}

	public MailingModelo buscar(Integer id) {
		System.out.println("--EXECUTANDO SERVIÇO - BUSCAR--");
		System.out.println("Buscando item de id: " + id);
		for (MailingModelo mailing : repositorio.listagem()) {
			if (mailing.id == id) {
				System.out.println("Cadastro encontrado");
				return mailing;
			}
		}
		System.out.println("Cadastro não encontrado");
		return null;
	}
	
	public boolean buscarLogin(String email) 
	{
		boolean resultado = false;
		System.out.println("--EXECUTANDO SERVIÇO - BUSCAR LOGIN--");
		System.out.println("Buscando item de email: " + email);
		for (MailingModelo mailing : repositorio.listagem()) 
		{
			System.out.println("String recebida " + email);
			System.out.println("String do repositorio " + mailing.email);
			if (mailing.email.equals(email)) 
			{
				System.out.println("Cadastro encontrado");
				resultado = true;
				System.out.println(resultado);
				return resultado;
			}{
				System.out.println("Cadastro não encontrado");
				resultado = false;
				System.out.println(resultado);
				return resultado;
			 }
		
		}
		return resultado;
	}
	
	public String deletar(Integer id) {
		MailingModelo mailing = buscar(id);
		if (mailing == null) {
			return "Mailing não encontrado";
		} else {
			
			repositorio.deletar(mailing);
			return "Mailing apagado com sucesso";
		}
	}

	public String atualizar(MailingModelo mailing) {
		if (mailing.nome.equals("")) {
			System.out.println("--Alteração de mailing reprovada, nome inválido--");
			return "--Alteração de mailing reprovada, nome inválido--";
		} else if (mailing.email.equals("")) {
			System.out.println("--Alteração de mailing reprovada, email inválido--");
			return "--Alteração de mailing reprovada, email inválido--";
		} else if (mailing.telefone.equals("")) {
			System.out.println("--Alteração de mailing reprovada, telefone inválido--");
			return "--Alteração de mailing reprovada, telefone inválido--";
		} else {
		System.out.println("--Alteração de mailing aprovada--");		
		repositorio.atualizar(mailing);
		return "--Alteração de mailing aprovada--";
	}}

}
