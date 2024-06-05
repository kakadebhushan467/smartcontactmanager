package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	private BCryptPasswordEncoder bCryptPassswordEncoder; 
	
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
		//Showing Particulr Contact details
		@RequestMapping("/{cId}/contact")
		public String showContactDetails(@PathVariable("cId") Integer cId,Model model,Principal principal) {
			System.out.println("CID"+cId);
			Optional<Contact> contactOption = this.contactRepository.findById(cId);
			Contact contact = contactOption.get();
			
			//We will get the loggedin user from below
			String userName = principal.getName();
			User user=this.userRepository.getUserByUserName(userName);
			if(user.getId()==contact.getUser().getId()) {
				model.addAttribute("contact",contact);
				model.addAttribute("title",contact.getName());
			}
			return "normal/contact_detail";
		}
		
		//Delete contact handler
		@GetMapping("/delete/{cid}")
		public String deleteContact(@PathVariable("cid") Integer cid,Model model,Principal principal,HttpSession session) {
			
			System.out.println("CID"+cid);
			Contact contact=this.contactRepository.findById(cid),get();
		
			System.out.println("Contact"+contact.getcId());
			contact.setUser(null);
			System.out.println("Deleted");
			     session.setAttribute("massage", new Massage("Contact deleted successfully","success"));
			
			return "redirect:/user/show-contacts/0";
		}
		
		//Open update form handler
		@PostMapping("/update-contact/{cid}")
		public String updateForm(@PathVariable("cid") Integer cid,Model model) {
			model.addAttribute("title","Update Contact");
			Contact contact = this.contactRepository.findById(cid).get();
			model.addAttribute("contact",contact);
			return "normal/update_form";
		}
		
		//update contact handler
		//update hanlder
		@RequestMapping(value="/process-update",method= RequestMethod.POST)
		public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
				Model m,HttpSession session,Principal principal) {
			try {
				Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
				//image..
				if(!file.isEmpty()) {
					//file work..
					
					//delete old photo
					File deleteFile=new ClassPathResource("static/img").getFile();
					File file1=new File(deleteFile,oldContactDetail.getImage());
					file1.delete();
					
					//update new photo
					File saveFile=new ClassPathResource("static/images").getFile();
				
					Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
					
					Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
					
					contact.setImage(file.getOriginalFilename());
				}else {
					contact.setImage(oldContactDetail.getImage());
				}
				
				User user=this.userRepository.getUserByUserName(principal.getName());
				contact.setUser(user);
				this.contactRepository.save(contact);
			
              session.setAttribute("message",new Massage("Your contact is updated","success"));
			
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("Contact Name"+contact.getName());
			System.out.println("Contact Id"+contact.getcId());
			return "redirect:/user/"+contact.getcId()+"/contact";
		}
		
		//your profile handler
		
		@GetMapping("/profile")
		public String yourProfile(Model model) {
			model.addAttribute("title","Profile page");
			return "normal/profile";
		}
		
		//opensetting handler
		@GetMapping("/settings")
		public String openSettings()
		{
			return "normal/settings";
		}
		
		//change password handler
		@PostMapping("/change-password")
		public String changePassword(@RequestParam("oldPassword") String oldPassword,
									 @RequestParam("newPassword") String newPassword,
									 Principal principal,
									 HttpSession session){
			System.out.println("Old Password "+oldPassword);
			System.out.println("New Password "+newPassword);
			String username=principal.getName();
			User currentUser = this.userRepository.getUserByUserName(username);
			System.out.println(currentUser.getPassword());
			if(this.bCryptPassswordEncoder.matches(oldPassword,currentUser.getPassword())){
				
				//change password
				currentUser.setPassword(this.bCryptPassswordEncoder.encode(newPassword));
				this.userRepository.save(currentUser);
				session.setAttribute("message",new Massage("Your password is updated","success"));
			}else{
				//error...
				session.setAttribute("message",new Massage("Please enter correct old password","danger"));
				return "redirect:/user/settings";
			}

			return "redirect:/user/index";
		}
		
}
