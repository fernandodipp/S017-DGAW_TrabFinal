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
	
	@RequestMapping(value= "/submitUpdate", method=RequestMethod.POST)
	public String atualizar(
			@RequestParam(name = "ID") String userid,
			@RequestParam(name = "nome") String nome,
			@RequestParam(name = "email") String email,
			@RequestParam(name = "telefone") String telefone,
			Model model){
		String resposta;
		int id = Integer.parseInt(userid);
		MailingServico mailingServico = new MailingServico();
		resposta = mailingServico.deletar(id);
		if (resposta == "Mailing não encontrado") {
			model.addAttribute("MENSAGEM", resposta);
			return listaTodosMailing();
		}else {
		MailingModelo mailing = new MailingModelo();		
		mailing.setId(id);
		mailing.setNome(nome);
		mailing.setEmail(email);
		mailing.setTelefone(telefone);
		resposta = mailingServico.atualizar(mailing);		
		switch (resposta) {
		case "--Alteração de mailing reprovada, nome inválido--":			
			model.addAttribute("MENSAGEM", resposta);
			return "erro-nome";
		case "--Alteração de mailing reprovada, email inválido--":
			model.addAttribute("MENSAGEM", resposta);
			return "erro-email";
		case "--Alteração de mailing reprovada, telefone inválido--":
			model.addAttribute("MENSAGEM", resposta);
			return "erro-telefone";
		case "--Alteração de mailing aprovada--":
			model.addAttribute("MENSAGEM", resposta);
			return "listagem-mailing";
		}}
		return null;
	}
	
	@RequestMapping(value="/atualizar/{id}", method=RequestMethod.GET)
	public String preparaAtualizar(@PathVariable("id") Integer id, Model model) {
		model.addAttribute("ID", id.toString());
		return "prepara-atualizar";
	}
	
	@RequestMapping(value = "/paginaLogin", method = RequestMethod.GET)
	public String preparaLogin() {		
		return "paginaLogin";
	}
	
	@RequestMapping(value = "/admin", method = RequestMethod.GET)
	public String preparaLoginAdmin() {		
		return "adminLogin";
	}
	
	@RequestMapping(value = "/administration", method = RequestMethod.POST)
	public String LogaAdmin(
			@RequestParam(name = "user") String user,
			@RequestParam(name = "password") String password,
			Model model) {
		MailingServico mailingServico = new MailingServico();
		boolean resposta = mailingServico.adminLog(user, password);
		if (resposta == true)
		{
			return listaTodosMailing();
		}
		return "adminLoginError";
	}
	
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public String loginMailing(
			@RequestParam(name = "email") String email, 
			Model model) {
		MailingServico servico = new MailingServico();
		boolean resposta = servico.buscarLogin(email);
		String mensagem;
		if (resposta == true) {
			mensagem = "Cadastro encontrado!";
			model.addAttribute("MENSAGEM", mensagem);
		return "sucesso";
		}
		else {
			return "erro-email";
		}
			
	}
	
	@RequestMapping(value = "/homeMailing", method = RequestMethod.GET)
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
