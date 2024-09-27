# Dust-Http
dust-http is a wrapper around OkHttp3 for http client services 
and the Tyrus web socket client so as to present an idiomatic Dust/Actor interface.

It is a part of the Dust (https://github.com/dust-ai-mr) Actor library for Java 21+.

The http client supports synchronous and asynchronous calls - the latter responses
being converted to Dust messages. It also supports Server Side events with 
open/close/data/error events again being converted to Dust messages.

For example here is a minimal Http client enabled Actor:
```groovy

	class CnnActor extends Actor implements HttpClientActor {

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
						stopSelf()
						break

					default: log.error "Got message $message"
				}
			}
		}

	}
```
It simply makes a call to cnn.com, dumps the html content and stops itself. See the tests for a 
simple WebsocketClientTest example.

For support contact alanl@mentalresonance.com
