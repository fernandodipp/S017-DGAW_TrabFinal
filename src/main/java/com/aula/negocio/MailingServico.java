package com.aula.negocio;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.aula.modelo.MailingModelo;

@Service
public class MailingServico {

	ArrayList<MailingModelo> lista = new ArrayList<>();
	static int contadorId = 1;

	public Integer salvar(MailingModelo mailing) {
		System.out.println("--EXECUTANDO MÉTODO SALVAR, PACOTE NEGÓCIO--");
		System.out.println("Cadastrando o id: " + mailing.id + ", nome: " + mailing.nome + ", email: " + mailing.email
				+ " e telefone: " + mailing.telefone);
		if (mailing.nome.equals("")) {
			System.out.println("--Adição de mailing reprovada, nome inválido--");
			return null;
		} else if (mailing.email.equals("")) {
			System.out.println("--Adição de mailing reprovada, email inválido--");
			return null;
		} else if (mailing.telefone.equals("")) {
			System.out.println("--Adição de mailing reprovada, telefone inválido--");
			return null;
		}
		System.out.println("--Adição de mailing aprovada--");
		mailing.id = contadorId++;
		lista.add(mailing);
		return mailing.id;
	}

	public ArrayList<MailingModelo> listar() {
		System.out.println("--EXECUTANDO MÉTODO LISTAR, PACOTE NEGÓCIO--");
		System.out.println("Listando todas as entradas:");
		for (MailingModelo i : lista)		
			System.out.println("ID: " + i.id + ", Nome: " + i.nome + ", Email: " + i.email + ", Telefone: " + i.telefone);
		return lista;
	}

	public MailingModelo buscar(Integer id) {
		System.out.println("--EXECUTANDO MÉTODO BUSCAR, PACOTE NEGÓCIO--");
		System.out.println("Buscando item de id: " + id);
		for (MailingModelo mailing : lista) {
			if (mailing.id == id) {
				System.out.println("Cadastro encontrado");
				return mailing;
			}
		}
		System.out.println("Cadastro não encontrado");
		return null;
	}

	public void deletar(Integer id) {
		System.out.println("--EXECUTANDO MÉTODO DELETAR, PACOTE NEGÓCIO--");
		System.out.println("Deletando item de id: " + id);
		lista.removeIf(obj -> obj.id == id);
	}

	public void atualizar(MailingModelo mailing) {
		System.out.println("--EXECUTANDO MÉTODO ATUALIZAR, PACOTE NEGÓCIO--");
		System.out.println("Atualizando o id: " + mailing.id);
		lista.removeIf(obj -> obj.id == mailing.id);
		lista.add(mailing);
	}

}
