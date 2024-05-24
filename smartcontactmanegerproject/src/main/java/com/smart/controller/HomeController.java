package com.smart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

 @Controller
 public class HomeController {
 @RequestMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title","Home-Smart contact manager");
		return "home";
	}
 @RequestMapping("/about")
	public String about(Model model)
	{
		model.addAttribute("about","Home-Smart contact manager");
		return "about";
	}
 @RequestMapping("/signup")
 public String signup(Model model)
{
	model.addAttribute("sihnup","register-Smart contact manager");
	return "signup";
}
}
