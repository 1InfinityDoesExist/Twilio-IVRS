package com.twilio.ivrs.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.plivo.api.exceptions.PlivoValidationException;
import com.plivo.api.exceptions.PlivoXmlException;
import com.twilio.ivrs.service.IVRSServiceImpl;

@RestController
public class HelperController {

	@Autowired
	private IVRSServiceImpl ivrSServiceImpl;

	@GetMapping("/test")
	public void testing(HttpServletRequest request, HttpServletResponse response) throws IOException {

		try {
			try {
				ivrSServiceImpl.doPost(request, response);
			} catch (URISyntaxException | InterruptedException | UnirestException | PlivoXmlException
					| PlivoValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
