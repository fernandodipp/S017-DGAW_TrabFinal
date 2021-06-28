package com.aula.rest;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.aula.modelo.MailingModelo;
import com.aula.negocio.MailingServico;

import io.swagger.annotations.ApiOperation;

@RestController
public class MailingAPI {

	// injecao de dependencia.
	@Autowired
	MailingServico servico;
	
	@ApiOperation(value="Salva os mailings", response = List.class)
	@RequestMapping(value= "/mailing", method=RequestMethod.POST, produces = "application/json")
	public ResponseEntity<Integer> salvar(@RequestBody MailingModelo mailing){		
		return new ResponseEntity<>(servico.salvar(mailing),HttpStatus.OK);		
	}
	
	@ApiOperation(value="Lista todos os mailings", response = MailingModelo.class)
	@RequestMapping(value= "/mailing", method=RequestMethod.GET, produces = "application/json")
	public ArrayList<MailingModelo> listar(){
		return servico.listar();
	}
	
	@ApiOperation(value="Busca um mailing pelo id", response = MailingModelo.class)
	@RequestMapping(value= "/mailing/{id}", method=RequestMethod.GET, produces = "application/json")
	public ResponseEntity<MailingModelo> buscar(@PathVariable("id") Integer id){
		try {
		return new ResponseEntity<>(servico.buscar(id),HttpStatus.OK);
		}catch(Exception e) {
			System.out.println("Este ID " + id + " não é reconhecido pela minha API");
			return null;
		}
	}
	
	@ApiOperation(value="Apaga o mailing pelo ID", response = MailingModelo.class)
	@RequestMapping(value= "/mailing/{id}", method=RequestMethod.DELETE, produces = "application/json")
	public ResponseEntity<Void> deletar(@PathVariable("id") Integer id){
		servico.deletar(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@ApiOperation(value="Atualiza um mailing", response = MailingModelo.class)
	@RequestMapping(value= "/mailing", method=RequestMethod.PUT, produces = "application/json")
	public ResponseEntity<Void> atualizar(@RequestBody MailingModelo mailing){		
		servico.atualizar(mailing);
		return new ResponseEntity<>(HttpStatus.OK);		
	}
	
}
