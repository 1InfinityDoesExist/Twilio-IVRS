package com.twilio.ivrs.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plivo.api.exceptions.PlivoValidationException;
import com.plivo.api.exceptions.PlivoXmlException;
import com.plivo.api.xml.Dial;
import com.plivo.api.xml.GetInput;
import com.plivo.api.xml.Number;
import com.plivo.api.xml.Response;
import com.plivo.api.xml.Speak;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class PlivoVoiceApplication {

	// Welcome message, first branch
	String WelcomeMessage = "Welcome to the demo. Press 1 for your account balance. Press 2 for your account status. Press 3 to speak to a representative";
	// Message for second branch
	String RepresentativeBranch = "Press 1 for sales. Press 2 for support";
	// Message that Plivo reads when the caller does nothing
	String NoInput = "Sorry, I didn't catch that. Please hang up and try again";
	// Message that Plivo reads when the caller presses a wrong digit
	String WrongInput = "Sorry, that's not a valid input";

	@GetMapping(value = "/multilevelivr/", produces = { "application/xml" })
	public Response getInput() {
		log.info("---------PlivoVoiceApplication Class, getInput method -----------");
		Response response;
		try {
			response = new Response().children(
					new GetInput().action("https://sengagement.herokuapp.com/multilevelivr/firstbranch/").method("POST")
							.inputType("dtmf").digitEndTimeout(5).redirect(true).children(new Speak(WelcomeMessage)))
					.children(new Speak(NoInput));
			System.out.println(response.toXmlString());
			return response;
		} catch (PlivoValidationException | PlivoXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	@RequestMapping(value = "/multilevelivr/firstbranch/", method = RequestMethod.POST, produces = {
			"application/xml" })
	public Response speak(@RequestParam("Digits") String digit) throws PlivoXmlException, PlivoValidationException {
		log.info("--------Digit pressed: {} ", digit);
		Response response = new Response();
		if (digit.equals("1")) {
			try {
				response.children(new Speak("Your account balance is $20"));
			} catch (PlivoValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (digit.equals("2")) {
			try {
				response.children(new Speak("Your account status is active"));
			} catch (PlivoValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (digit.equals("3")) {
			try {
				response.children(new GetInput().action("https://sengagement.herokuapp.com/multilevelivr/second/")
						.method("POST").inputType("dtmf").digitEndTimeout(5).redirect(true)
						.children(new Speak(RepresentativeBranch))).children(new Speak(NoInput));
			} catch (PlivoValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			response.children(new Speak(WrongInput));
		}
		System.out.println(response.toXmlString());
		return response;
	}

	@RequestMapping(value = "/multilevelivr/second/", produces = { "application/xml" }, method = RequestMethod.POST)
	public Response callforward(@RequestParam("Digits") String digit, @RequestParam("From") String from_number)
			throws PlivoXmlException, PlivoValidationException {
		log.info("--------Digit pressed: {}", digit);
		Response response = new Response();
		if (digit.equals("1")) {
			response.children(new Dial().action("https://sengagement.herokuapp.com/multilevelivr/action/")
					.method("POST").redirect(false).children(new Number("<number_1>")));
		} else if (digit.equals("2")) {
			response.children(new Dial().action("https://sengagement.herokuapp.com/multilevelivr/action/")
					.method("POST").redirect(false).children(new Number("<number_2>")));
		} else {
			response.children(new Speak(WrongInput));
		}
		System.out.println(response.toXmlString());
		return response;
	}
}
