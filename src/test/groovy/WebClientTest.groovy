/*
 * Copyright 2024 Alan Littleford
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


import com.mentalresonance.dust.core.actors.Actor
import com.mentalresonance.dust.core.actors.ActorBehavior
import com.mentalresonance.dust.core.actors.ActorSystem
import com.mentalresonance.dust.core.actors.Props
import com.mentalresonance.dust.http.service.HttpRequestResponseMsg
import com.mentalresonance.dust.http.trait.HttpClientActor
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import okhttp3.Response
import spock.lang.Specification

/**
 * Trivial client - sends a Get request to a website and print out the html it got.
 */
@CompileStatic
class WebClientTest extends Specification {

	public static boolean gotdata = false

	@Slf4j
	static class CnnActor extends Actor implements HttpClientActor {

		static Props props() {
			Props.create(CnnActor.class)
		}

		@Override
		void preStart() {
			request('https://cnn.com')
		}

		@Override
		ActorBehavior createBehavior() {
			(Serializable message) -> {
				switch(message) {
					case HttpRequestResponseMsg:
						Response response = ((HttpRequestResponseMsg)message).response
						log.info response.body().string()
						gotdata = true
						stopSelf()
						break

					default: log.error "Got message $message"
				}
			}
		}

	}
	def "Finnhub"() {
		when:
			ActorSystem system = new ActorSystem("Test")
			system.context.actorOf(CnnActor.props()).waitForDeath()
			system.stop()
		then:
			gotdata
	}

}