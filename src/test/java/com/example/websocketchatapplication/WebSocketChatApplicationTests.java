package com.example.websocketchatapplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(WebSocketChatApplication.class)

public class WebSocketChatApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void login() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk())
				.andExpect(content().string(containsString("Chat Room")))
				.andExpect(view().name("login"));
	}

	@Test
	public void index() throws Exception {
		mockMvc.perform(get("/index?")).andExpect(status().isOk())
				.andExpect(content().string(containsString("Welcome")))
				.andExpect(view().name("chat"));
	}

}
