package verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class CommReceiverBroadcast extends AbstractVerticle {
	
	public void start(Future<Void> startFuture) { //Receptor
		
		vertx.eventBus().consumer("mensaje-broadcast", message -> {
			System.out.println("Eres Broadcast: " + message.body().toString());
			System.out.println("\n Si , eres Broadcast");
			
		});
	}

}
