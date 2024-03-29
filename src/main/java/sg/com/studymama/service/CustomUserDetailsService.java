package sg.com.studymama.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import sg.com.studymama.DTO.UserDTO;
import sg.com.studymama.exceptions.UserAlreadyExistAuthenticationException;
import sg.com.studymama.exceptions.UserEmailVerificationException;
import sg.com.studymama.exceptions.UserRoleException;
import sg.com.studymama.model.DAOUser;
import sg.com.studymama.model.DAOUserProfile;
import sg.com.studymama.repository.UserProfileRepository;
import sg.com.studymama.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private static final Logger LOG = LoggerFactory.getLogger(CustomUserDetailsService.class);
	private static final List<String> ROLES = new ArrayList<String>(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));

	@Autowired
	private UserRepository userDao;

	@Autowired
	private UserProfileRepository userProfileDao;

	@Autowired
	private PasswordEncoder bcryptEncoder;
	
	@Autowired
    private EmailService emailService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, UserEmailVerificationException {
		List<SimpleGrantedAuthority> roles = null;
		DAOUser user = userDao.findByUsername(username);
		if (user != null) {
			LOG.info(user.toString());
			if(user.getEmailVerified()) {
				roles = Arrays.asList(new SimpleGrantedAuthority(user.getRole()));
				return new User(user.getUsername(), user.getPassword(), roles);
			} else 
				throw new UserEmailVerificationException("User email not verified. Please verify email! " + username);
			
		}
		throw new UsernameNotFoundException("User not found with username: " + username);
	}

	public DAOUser save(UserDTO user) throws UserAlreadyExistAuthenticationException, MessagingException {
		if (!ROLES.contains(user.getRole()))
			throw new UserRoleException("Wrong role selection");
		if (userDao.findByUsername(user.getUsername()) == null) {
			DAOUser newUser = new DAOUser();
			newUser.setUsername(user.getUsername());
			newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
			newUser.setEmail(user.getEmail());
			newUser.setEmailVerified(false);
			newUser.setRole(user.getRole());
			DAOUserProfile newProfile = new DAOUserProfile();
			userProfileDao.save(newProfile);
			newUser.setUser_profile_id(newProfile.getId());
			LOG.info("save " + newUser.toString());
			emailService.sendEmailVerifyLink(user.getEmail(), user.getUsername());
			return userDao.save(newUser);
		}
		throw new UserAlreadyExistAuthenticationException("username already exists: " + user.getUsername());
	}
	
	public DAOUser verifyEmail(UserDTO user) {
		DAOUser daoUser = userDao.findByUsername(user.getUsername());
		if (daoUser == null)
			throw new UsernameNotFoundException("User not found with username: " + user.getUsername());
		else {
			if (!ROLES.contains(daoUser.getRole()))
				throw new UserRoleException("Wrong role selection");
			daoUser.setEmailVerified(true);
			return userDao.save(daoUser);
		}
	}

	public DAOUser find(String username) throws UsernameNotFoundException {
		DAOUser user = userDao.findByUsername(username);
		if (user != null) {
			LOG.info("find " + user.toString());
			return user;
		}
		throw new UsernameNotFoundException("User not found with username: " + username);
	}

}
