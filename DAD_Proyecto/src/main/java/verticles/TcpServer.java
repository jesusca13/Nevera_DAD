package verticles;

import java.util.Random;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

public class TcpServer extends AbstractVerticle {

	public void start(Future<Void> startFuture) {

		NetServerOptions netServerOptions = new NetServerOptions();
		netServerOptions.setPort(8086);
		// netServerOptions.setIdleTimeout(idleTimeout); //por defecto 120s
		NetServer netServer = vertx.createNetServer(netServerOptions);// desplegar servidor con las opciones que le
																		// hemos indicado
		// conexion
		
		netServer.connectHandler(connection -> { 

			JsonObject jsonObject = new JsonObject(); // close (cierra conexion) end (cierra peticion)
			jsonObject.put("body", "Conexion realizada correctamente");
			jsonObject.put("status", 200);
			jsonObject.put("clienteAddress", connection.remoteAddress().toString()).put("ServerAddress",
					connection.localAddress().toString());
			
			System.out.println("server: " + jsonObject.encode());
			
			connection.write(jsonObject.encodePrettily());
			
		// respuesta

			connection.handler(request -> { 

				Random random = new Random();
				JsonObject response = new JsonObject();
				JsonObject resquestMsg = request.toJsonObject();

				if (resquestMsg.containsKey("req")) {

					switch (resquestMsg.getInteger("req")) {

					case 1:
						response.put("response", "El dispositivo se ha encendido correctamente");
						response.put("status", 200);
						response.put("ClientAddress", connection.remoteAddress().toString());

					case 2:
						response.put("response", "Valor obtenido");

						if (resquestMsg.getString("content").equals("temperatura")) {
							response.put("value", 10 + random.nextInt(20)); // valor de un sensor externo / base de datos

						} else if (resquestMsg.getString("content").equals("pressure")) {
							response.put("value", 1000 + random.nextInt(50));

						} else if (resquestMsg.getString("content").equals("humidity")) {
							response.put("value", 40 + random.nextInt(60));

						} else {
							response.put("value", -1);
						}

						response.put("status", 200);
						response.put("ClientAddress", connection.remoteAddress().toString());
						break;

					default:
						response.put("response", "El dispositivo se ha reseteado");
						response.put("status", 200);
						response.put("ClientAddress", connection.remoteAddress().toString());
						break;
					}

				} else {
					response.put("response", " ");
					response.put("status", 300);
					response.put("ClientAddress", connection.remoteAddress().toString());
				}
				
				System.out.println("server: " + response.encodePrettily());
				connection.write(response.encode());

			});
		});
		
		// hadler que determina si despliegue se ha hecho correctamete
		
		netServer.listen(deploy -> {

			if (deploy.succeeded()) {
				System.out.println("Servidor desplegado correctamente");
				startFuture.complete(); // comunicacion con core
			} else {
				System.out.println(deploy.cause().getMessage());
				startFuture.fail(deploy.cause());
			}
		});

	}

}
