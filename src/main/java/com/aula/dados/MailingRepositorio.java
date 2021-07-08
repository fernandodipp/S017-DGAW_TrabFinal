package com.aula.dados;

import java.util.ArrayList;

import com.aula.controller.*;
import com.aula.dados.*;
import com.aula.modelo.*;
import com.aula.negocio.*;
public class MailingRepositorio {

	static public ArrayList<MailingModelo> mailings = new ArrayList<>();
	static int contadorId = 1;

	public void salvar(MailingModelo mailing) {
		mailing.id = contadorId++;
		System.out.println("Repositorio/Salvando mailing em repositório");
		System.out.println("Cadastrando o id: " + mailing.id + ", nome: " + mailing.nome + ", email: " + mailing.email
				+ " e telefone: " + mailing.telefone);
		MailingModelo mailingNovoRegistro = new MailingModelo();
		mailingNovoRegistro.setId(mailing.id);
		mailingNovoRegistro.setNome(mailing.nome);
		mailingNovoRegistro.setEmail(mailing.email);
		mailingNovoRegistro.setTelefone(mailing.telefone);
		mailings.add((MailingModelo) mailingNovoRegistro);
		listagem();
	}

	public ArrayList<MailingModelo> listagem() {
		System.out.println("Repositorio/Listando todas as entradas:");		
		  for (MailingModelo mailingNovoRegistro : mailings) 
		  {
			  System.out.println("ID: " + mailingNovoRegistro.id +", Nome: " + mailingNovoRegistro.nome + ", "
			  		+ "Email: " + mailingNovoRegistro.email + ", Telefone: " + mailingNovoRegistro.telefone);
		  }
		System.out.println(mailings.size());
		return mailings;
	}
	
	public void deletar(MailingModelo mailing) {
		System.out.println("Repositorio/Apagando mailing determinado:");
		System.out.println("Deletando item de id: " + mailing.id);
		mailings.remove(mailing);
		}

	public void atualizar(MailingModelo mailing) {
		System.out.println("Repositorio/Alterando mailing em repositório");
		System.out.println("Atualizando a entrada com o id: " + mailing.id);
		MailingModelo mailingNovoRegistro = new MailingModelo();
		mailingNovoRegistro.setId(mailing.id);
		mailingNovoRegistro.setNome(mailing.nome);
		mailingNovoRegistro.setEmail(mailing.email);
		mailingNovoRegistro.setTelefone(mailing.telefone);
		mailings.add((MailingModelo) mailingNovoRegistro);
		listagem();
	}
	
}
