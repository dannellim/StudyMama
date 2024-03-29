package sg.com.studymama.controller;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.warrenstrange.googleauth.GoogleAuthenticator;

import sg.com.studymama.service.EmailService;
import sg.com.studymama.util.CryptoUtils;

@RestController
public class TestController {

	private static final Logger LOG = LoggerFactory.getLogger(TestController.class);
	
	@Autowired
    private EmailService emailService;

	@RequestMapping(value = "/hellouser", method = RequestMethod.GET)
	public String helloUser() {
		LOG.info("HELLO USER");
		return "Hello User";
	}

	@RequestMapping(value = "/helloadmin", method = RequestMethod.GET)
	public String helloAdmin() {
		LOG.info("HELLO ADMIN");
		return "Hello Admin";
	}

	@RequestMapping(value = "/greeting", method = RequestMethod.GET)
	public @ResponseBody String greeting() {
		return "Hello, World. Welcome to StudyMama!";
	}

	@RequestMapping(value = "/demo", method = RequestMethod.GET)
	public @ResponseBody String demo() {
		return "Hello! This is a demo.";
	}

	@RequestMapping(value = "/sendemail", method = RequestMethod.GET)
	public String sendEmail(@RequestParam String email) {
		try {
			emailService.testEmail(email);
		} catch (MessagingException e) {
			LOG.error("Send email error: ", e);
			return "Email sending failed";
		}
		return "Email sent successfully";
	}
	
	@RequestMapping(value = "/encrypt", method = RequestMethod.GET)
	public String encrypt(@RequestParam String data) {
		return CryptoUtils.encrypt(data);
	}
	
	@RequestMapping(value = "/decrypt", method = RequestMethod.GET)
	public String decrypt(@RequestParam String data) {
		return CryptoUtils.decrypt(data);
	}
	
	@RequestMapping(value = "/testGenerateOTP", method = RequestMethod.GET)
	public String testGenerateOTP(String secret) {
		GoogleAuthenticator gAuth = new GoogleAuthenticator();
		int code = gAuth.getTotpPassword(secret);
		return String.valueOf(code);
	}
	
	@RequestMapping(value = "/testVerifyOTP", method = RequestMethod.GET)
	public String testVerifyOTP(String secret, String otp) {
		boolean authorizedOtp = false;
		GoogleAuthenticator gAuth = new GoogleAuthenticator();
		authorizedOtp = gAuth.authorize(secret,
				Integer.parseInt(otp));
		return String.valueOf(authorizedOtp);
	}
}
