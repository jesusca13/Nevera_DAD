package verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class CommReceiver extends AbstractVerticle {
	
	public void start(Future<Void> startFuture) { //Receptor
		
		vertx.eventBus().localConsumer("mensaje-punto-a-punto", message -> {
			System.out.println(message.body().toString());
			String response = "Si, yo estoy aqui " + this.getClass().getName(); //Devuelve nombre del class
			//se pueden implemetar filtros para bloqueos de emisores
			
			message.reply(response); //respuesta
		});
	}

}
