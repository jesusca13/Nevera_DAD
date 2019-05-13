package verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
//import io.vertx.core.http.HttpServer;

public class Main extends AbstractVerticle { // Es un verticle

	public void start(Future<Void> startFuture) { // starFuture -> comunicacion con core

		// vertx.deployVerticle(new CommReceiver());
		// vertx.deployVerticle(new CommSender());
		// vertx.deployVerticle(new CommReceiverBroadcast());
		// vertx.deployVerticle(new jsonExamples());
		//vertx.deployVerticle(new TcpServer());
		//vertx.deployVerticle(new TcpClient());
		vertx.deployVerticle(new RestServer());
		vertx.deployVerticle(new MqttExample());
		

		// No se permite lanzar varios verticle en paralelo, para ello desplegamos

		vertx.createHttpServer().requestHandler(request -> { // r es el parametro que le pasamos
			request.response().end("<p> Hola mundo </p>"); // Respuesta a la peticion
			
		}).listen(8081, status -> { // (status-> stado del servidor)
			
			if (status.succeeded()) {
				System.out.println("Servidor Http desplegado\n");
				startFuture.succeeded(); // si ha completado en parte .complete()
			} else {
				System.out.println("Error en el despliegue" + status.cause().getMessage());
				startFuture.fail(status.cause());
			}
		});

	}

}