package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Massage;


import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserRepository contactRepository;
	
	//method for adding common data to responce
	
	@ModelAttribute
	public void assCommonData(Model model,Principal principal) {
		String userName=principal.getName();
		System.out.println("USERNAME+userName");
		
		// get the user username(Email)
		User user=userRepository.getUserByUserName(userName);
		System.out.println("USER"+user);
		model.addAttribute("user",user);
		
	}
	
	//dashboard-home
@RequestMapping("/index")
public String dashboard(Model model,Principal principal)
{
	
	model.addAttribute("title","User Dashboard");
	
	return"normal/user_dashboard";
}

//Open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
//processing and contact form
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact ,
			@RequestParam("profileImage")
			MultipartFile file,
			Principal principal ,HttpSession session) {
		try {
		String name=principal.getName()	;	
		User user=this.userRepository.getUserByUserName(name);
		
		//proccesing and uploading file
		if(file.isEmpty())
		{
			
		}else
		{
			contact.setImage(file.getOriginalFilename());
			File saveFile=new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is uploaded");
		}
			
		
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("DATA"+contact);
		
		System.out.println("Added to data base");
		
		//message success
		session.setAttribute("massage", new Massage("Your contact is added !! And more..","success"));
		
		}catch (Exception e) {
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
			session.setAttribute("massage", new Massage("Some went Wrong !1 try again..","danger"));
	}
		return"normal/add_contact_form";
	}
	//Show Contacts Handler
		//per page =5[n]
		//current page=0 [page]
		@GetMapping("/show-contacts/{page}")
		public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
			m.addAttribute("title","Show Contacts");
			//List of contacts will send from here
			String userName=principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			//contacts per page
			//current page
			Pageable pageable = PageRequest.of(page,5);
			Page<Contact> contacts = this.contactRepository.findContactsbyUser(user.getId(),pageable);
			m.addAttribute("contacts",contacts);
			m.addAttribute("currentPage",page);
			m.addAttribute("totalpages",contacts.getTotalPages());
			return "normal/show_contacts";
		}
}
