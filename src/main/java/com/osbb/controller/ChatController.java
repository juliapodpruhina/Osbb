package com.osbb.controller;


import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.osbb.model.User;
import com.osbb.model.chats.Chat;
import com.osbb.model.chats.MTest;
import com.osbb.model.chats.Message;
import com.osbb.service.ChatService;
import com.osbb.service.MessageService;
import com.osbb.service.UserProfileService;
import com.osbb.service.UserService;

@Controller
@SessionAttributes("roles")
@RequestMapping("/chats")
public class ChatController {

	@Autowired
	UserService userService;
	 
	@Autowired
	UserProfileService userProfileService;
	
	@Autowired
	MessageSource messageSource;
	
	@Autowired
	ChatService chatService;
	
	@Autowired
	MessageService messageService;
	private String getPrincipal(){
		String userName = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (principal instanceof UserDetails) {
			userName = ((UserDetails)principal).getUsername();
		} else {
			userName = principal.toString();
		}
		return userName;
	}
	@RequestMapping(value = { "/", "/all" }, method = RequestMethod.GET)
	public String listUsers(ModelMap model) {

		List<Chat> chats = chatService.getAllChats(userService.findBySSO(getPrincipal()).getId());
		model.addAttribute("chats", chats);
		model.addAttribute("loggedinuser", getPrincipal());
		return "chats";
	}
	@RequestMapping(value = { "/", "/alljson" }, method = RequestMethod.GET)
	@ResponseBody 
	public List<Chat> chats(ModelMap model) {

		List<Chat> chats = chatService.getAllChats(1);
		//List<Chat> chats = chatService.getAllChats(userService.findBySSO(getPrincipal()).getId());
		model.addAttribute("chats", chats);
		model.addAttribute("loggedinuser", getPrincipal());
		return chats;
	}
	
	@RequestMapping(value = { "/", "/{chatid}" }, method = RequestMethod.GET)
	public String chatMessages(@PathVariable int chatid, ModelMap model) {

		System.out.println("chat id ------------------ " + chatid);
		List<Message> messages = chatService.getChatMessages(chatid);

		model.addAttribute("messages", messages);
		Chat chat = chatService.getChatById(chatid);
		model.addAttribute("chat", chat);
		model.addAttribute("loggedinuser", getPrincipal());
		
		return "chat_view";
		
	}
	
	@MessageMapping("/chat/{id}")
    @SendTo("/topic/messages/{id}")
    public Message mess(@DestinationVariable String id,final Message mess) throws Exception {
		mess.setDateAdded(new Date());
		
		messageService.saveMessage(mess);
		
       return mess;
        
    }
}
