package com.Issuer.Issuer.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexCont {

	@RequestMapping("/")
    public String index() {
        return "Greetings from ISSUER!";
    }
	
}