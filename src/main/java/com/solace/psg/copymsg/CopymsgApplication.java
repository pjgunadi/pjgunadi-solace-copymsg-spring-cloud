package com.solace.psg.copymsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.StaticMessageHeaderAccessor;
import org.springframework.integration.acks.AckUtils;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SpringBootApplication
public class CopymsgApplication {

	private static final Logger logger = LoggerFactory.getLogger(CopymsgApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(CopymsgApplication.class, args);
	}

	@Bean
	public Consumer<Message<String>> myStreamBridge(StreamBridge sb) {
		return s -> {
			logger.info("Received: " + s.getPayload());
			// Disable Auto-Ack
			AcknowledgmentCallback ackCallback = StaticMessageHeaderAccessor.getAcknowledgmentCallback(s);
			ackCallback.noAutoAck();
			String strDestination = s.getHeaders().get("solace_destination").toString();
			logger.info("Destination: " + strDestination);
			Message<String> msg = MessageBuilder.withPayload(s.getPayload()).setHeader(BinderHeaders.TARGET_DESTINATION, strDestination).build();
			logger.info("Forwarding message");
			sb.send("myStreamBridge-out-0", msg);
			logger.info("Forwarded message");
			logger.info("Accepting Message");
			AckUtils.accept(ackCallback);
		};
	}

}
