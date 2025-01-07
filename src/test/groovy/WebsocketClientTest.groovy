/*
 * Copyright 2024-2025 Alan Littleford
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */


import com.mentalresonance.dust.core.actors.ActorSystem
import com.mentalresonance.dust.core.actors.Props
import com.mentalresonance.dust.http.actors.WebsocketClientActor
import groovy.util.logging.Slf4j
import spock.lang.Specification

import javax.websocket.CloseReason
import javax.websocket.Session

/**
 * Open a websocket to finnhub and print out some traceds for 5 seconds
 */
@Slf4j
class WebsocketClientTest extends Specification {

	public static boolean gotdata = false

	static class FinnActor extends WebsocketClientActor {

		static Props props(URI uri) {
			Props.create(FinnActor.class, uri);
		}

		@Override
		void preStart() {
			sendMessage('{"type":"subscribe","symbol":"BINANCE:BTCUSDT"}')
		}

		FinnActor(URI uri) {
			super(uri)
		}

		@Override
		void onOpen(Session userSession) {
			log.info "Opened connection"
		}

		@Override
		void onClose(Session userSession, CloseReason reason) {
			log.info "Closed connection"
		}

		@Override
		void onMessage(String message) {
			gotdata = true
			log.info message
		}
	}
	def "Finnhub"() {
		when:
			ActorSystem system = new ActorSystem("Test")
			system.context.actorOf(FinnActor.props(new URI("wss://ws.finnhub.io/?token=ckuo139r01qmtr8lehf0ckuo139r01qmtr8lehfg")))
			Thread.sleep(5000L)
			system.stop()
		then:
			gotdata
	}

}