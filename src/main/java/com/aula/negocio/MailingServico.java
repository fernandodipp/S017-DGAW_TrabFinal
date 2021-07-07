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
	
	public String deletar(Integer id) {
		MailingModelo mailing = buscar(id);
		if (mailing == null) {
			return "Mailing não encontrado";
		} else {
			
			repositorio.deletar(mailing);
			return "Mailing apagado com sucesso";
		}
	}

	public void atualizar(MailingModelo mailing) {
		System.out.println("--EXECUTANDO SERVIÇO - ATUALIZAR--");
		System.out.println("Atualizando o id: " + mailing.id);
		repositorio.listagem().removeIf(obj -> obj.id == mailing.id);
		repositorio.listagem().add(mailing);
	}

}
