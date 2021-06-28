package com.aula.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MailingController {
	@RequestMapping(value = "/mailing-cadastra", method = RequestMethod.GET)
	public String mostraMensagem() {	
		return "mailingHome";
	}

}
