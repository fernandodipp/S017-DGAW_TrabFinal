package com.aula.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aula.controller.*;
import com.aula.dados.*;
import com.aula.modelo.*;
import com.aula.negocio.*;
import com.aula.modelo.MailingModelo;
import com.aula.negocio.MailingServico;

@Controller
public class MailingController {
	
//	MailingServico servico = new MailingServico();
	
	@RequestMapping(value = "/apagar/{id}", method = RequestMethod.GET)
	public String apagar(@PathVariable("id") Integer id, Model model) {	
		MailingServico mailingServico = new MailingServico();
		String resposta = mailingServico.deletar(id);
		switch (resposta) {
		case "Mailing não encontrado":			
			model.addAttribute("MENSAGEM", resposta);
			return listaTodosMailing();
		case "Mailing apagado com sucesso":
			model.addAttribute("MENSAGEM", resposta);
		return listaTodosMailing();
		
	}	return null; }
	
	@RequestMapping(value = "/mailing-listagem", method = RequestMethod.GET)
	public String listaTodosMailing() {	
		//servico.listar();
		return "listagem-mailing";
	}	
	
	@RequestMapping(value= "/mailing/{id}", method=RequestMethod.GET)
	public String atualizar(@PathVariable("id") Integer id){		
		//MailingServico servico = new MailingServico();
		//MailingModelo mailing = servico.buscar(id);
		//servico.atualizar(mailing);
		return "atualiza-mailing";
	}
	
	@RequestMapping(value = "/mailing-formulario", method = RequestMethod.GET)
	public String preparaCadastroMailing() {		
		return "mailingHome";
	}	
	
	@RequestMapping(value = "/mailing", method = RequestMethod.POST)
	public String cadastraMailing(
			@RequestParam(name = "nome") String nome,
			@RequestParam(name = "email") String email,
			@RequestParam(name = "telefone") String telefone,
			Model model) {	
		System.out.println("Abrindo endpoint /mailing");
		MailingModelo mailing = new MailingModelo();
		MailingServico mailingServico = new MailingServico();
		mailing.setNome(nome);
		mailing.setEmail(email);
		mailing.setTelefone(telefone);
		//mailingServico.salvar(mailing);
		
		String resposta = mailingServico.salvar(mailing);		
		
		switch (resposta) {
		case "--Adição de mailing reprovada, nome inválido--":			
			model.addAttribute("MENSAGEM", resposta);
			return "erro-nome";
		case "--Adição de mailing reprovada, email inválido--":
			model.addAttribute("MENSAGEM", resposta);
			return "erro-email";
		case "--Adição de mailing reprovada, telefone inválido--":
			model.addAttribute("MENSAGEM", resposta);
			return "erro-telefone";
		case "--Adição de mailing aprovada--":
			model.addAttribute("MENSAGEM", resposta);
			return "sucesso";
		}
		return null;
		}
	
}
