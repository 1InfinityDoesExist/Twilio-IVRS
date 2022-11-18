package com.twilio.ivrs.service;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.plivo.api.exceptions.PlivoValidationException;
import com.plivo.api.exceptions.PlivoXmlException;
import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Call.Status;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Pause;
import com.twilio.twiml.voice.Redirect;
import com.twilio.twiml.voice.Say;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IVRSServiceImpl {

	// Welcome message, first branch
	String WelcomeMessage = "Welcome to the demo. Press 1 for your account balance. Press 2 for your account status. Press 3 to speak to a representative";
	// Message for second branch
	String RepresentativeBranch = "Press 1 for sales. Press 2 for support";
	// Message that Plivo reads when the caller does nothing
	String NoInput = "Sorry, I didn't catch that. Please hang up and try again";
	// Message that Plivo reads when the caller presses a wrong digit
	String WrongInput = "Sorry, that's not a valid input";

	public static final String ACCOUNT_SID = "ACe26536586ee5bd01fee8b547216daf12";
	public static final String AUTH_TOKEN = "d144965c634727715acb3098cac081e1";

	String from = "+18588081170";
	String to = "+917903262528";

	public void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException,
			URISyntaxException, InterruptedException, UnirestException, PlivoXmlException, PlivoValidationException {

		log.info("---------IVRSServiceImpl class, doPost methd : -------");
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

		VoiceResponse response = getPlanets();
		// Response resp = getPlanet();

		Call call = Call.creator(new PhoneNumber(to), new PhoneNumber(from), new Twiml(response.toXml())).create();

		String statusGet = "https://api.twilio.com/2010-04-01/Accounts/ACe26536586ee5bd01fee8b547216daf12/Calls/test.json";
		statusGet = statusGet.replace("test", call.getSid());

		log.info("----------Account sid : {}", call.getSid());
		String stat = "";
		while (!stat.equalsIgnoreCase(Status.IN_PROGRESS.toString())) {
			Unirest.setTimeouts(0, 0);
			HttpResponse<String> res = Unirest.get(statusGet).header("Authorization",
					"Basic QUNlMjY1MzY1ODZlZTViZDAxZmVlOGI1NDcyMTZkYWYxMjpkMTQ0OTY1YzYzNDcyNzcxNWFjYjMwOThjYWMwODFlMQ==")
					.asString();

			JSONObject jsonObject = null;
			try {
				jsonObject = (JSONObject) new JSONParser().parse(res.getBody());
				stat = (String) jsonObject.get("status");
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		servletResponse.setContentType("text/xml");
		try {
			servletResponse.getWriter().write(response.toXml());
		} catch (TwiMLException e) {
			throw new RuntimeException(e);
		}
	}

	private VoiceResponse getInitialInstructions() {

		VoiceResponse response = new VoiceResponse.Builder()
				.say(new Say.Builder("Hello frands chai pelow.").voice(Say.Voice.ALICE).language(Say.Language.EN_GB)
						.build())
				.say(new Say.Builder("Thank you for calling the ET Phone Home Service - the "
						+ "adventurous alien's first choice in intergalactic travel").build())
				.pause(new Pause.Builder().build()).build();

		return response;
	}

	private VoiceResponse getReturnInstructions() {

		VoiceResponse response = new VoiceResponse.Builder()
				.say(new Say.Builder("To get to your extraction point, get on your bike and go down "
						+ "the street. Then Left down an alley. Avoid the police cars. Turn left "
						+ "into an unfinished housing development. Fly over the roadblock. Go "
						+ "passed the moon. Soon after you will see your mother ship.").voice(Say.Voice.ALICE)
								.language(Say.Language.EN_GB).build())
				.say(new Say.Builder("Thank you for calling the ET Phone Home Service - the "
						+ "adventurous alien's first choice in intergalactic travel").build())
				.pause(new Pause.Builder().build()).build();

		return response;
	}

	private VoiceResponse getPlanets() {

//		VoiceResponse response = new VoiceResponse.Builder()
//				.say(new Say.Builder("Hello frands ----Press 1 or press 2").build())
//				.gather(new Gather.Builder().debug(true).numDigits(1).timeout(50)
//						.action("http://127.0.0.1:9200/multilevelivr/").method(HttpMethod.GET).build())
//				.build();

		log.info("-------IVRSServiceImpl Class, getPlanets method-----");
		return new VoiceResponse.Builder()
				.gather(new Gather.Builder().numDigits(1)
						.say(new Say.Builder("For sales, press 1. For support, press 2.").build()).build())
				.redirect(new Redirect.Builder("/multilevelivr/").build()).build();

	}

	private VoiceResponse getPlanet() throws PlivoValidationException {
		return new VoiceResponse.Builder()
				.redirect(new Redirect.Builder("/multilevelivr/").method(HttpMethod.GET).build())

				.build();
	}

}
