package com.smart.controller;


import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;



@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userrepository;
	
	//handler for home page
@RequestMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title","Home-Smart contact manager");
		return "home";
	}
//handler for the about page
@RequestMapping("/about")
	public String about(Model model)
	{
		model.addAttribute("about","about-Smart contact manager");
		return "about";
	}

//handler for sign up page
@RequestMapping("/signup")
public String signup(Model model)
{
	model.addAttribute("sihnup","signup-Smart contact manager");
	model.addAttribute("user",new User());
	return "signup";
}
//handler for registering user
@RequestMapping(value= "/do_register",method = RequestMethod.POST)
public String registerUser(@Valid @ModelAttribute("user")User user,BindingResult result1,@RequestParam(value="agreement",defaultValue="false")
boolean agreement,Model model,HttpSession session)
{
	try {
		
		if(agreement)
		{
			System.out.println("You have not agreed the term and condition");
			throw new Exception("You have not agreed the term and condition");
		}
		
		if(result1.hasErrors())
		{    System.out.println("Error"+result1.toString());
			model.addAttribute("user",user);
			return"signup";
					
		}
		
		user.setRole("ROLE_USER");
		user.setEnabled(true);
		user.setImageUrl("default.png");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		
		System.out.println("Agreement"+agreement);
		System.out.println("User"+user);
		
		User result=this.userrepository.save(user);
		
		model.addAttribute("user",new User());	
		session.setAttribute("message",new Message("successfully register","alert-sucess", null, null, null, null));
		return"signup";
		
	}catch (Exception e) {
		e.printStackTrace();
		model.addAttribute("user",user);
		session.setAttribute("message",new Message("something went wrong !!"+e.getMessage(),"alert-danger", null, null, e, null));
	}
		return"signup";
	}
	
	//handlere for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title","Login Page");
		return "login";
}
}



