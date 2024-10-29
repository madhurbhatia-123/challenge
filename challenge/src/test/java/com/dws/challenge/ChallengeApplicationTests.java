package com.dws.challenge;

import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ChallengeApplicationTests {
	@MockBean
	private NotificationService notificationService;

	@Autowired
	private AccountsService accountsService;
	@Test
	void contextLoads() {
	}

}
