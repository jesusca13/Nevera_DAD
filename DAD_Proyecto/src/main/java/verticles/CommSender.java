package verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

public class CommSender extends AbstractVerticle{ //Emisor
		
	public void start(Future<Void> startFuture) {
		
		EventBus eventBus = vertx.eventBus();
		vertx.setPeriodic(2000, tick -> {
			
			//mensaje Broadcast
			
			eventBus.publish("mensaje-broadcast", "Soy un Broadcast");  //como no hay handler no hay control sobre el mensaje
			
			//mensaje peer to peer 
			
			eventBus.send("mensaje-punto-a-punto", "Hola, ¿Hay alguien ahi?", response -> {     //(id, mensaje, handler)
				if(response.succeeded()) {
					System.out.println(response.result().body().toString());
				}else {
					System.out.println(response.cause().getMessage());
				}
			});    
		});
		
	}
	 
}
