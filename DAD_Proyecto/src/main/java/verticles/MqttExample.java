package verticles;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
//import io.vertx.ext.sql.SQLClient;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttPublishMessage;

public class MqttExample extends AbstractVerticle {

	private static Multimap<String, MqttEndpoint> clientTopics;

	public void start(Future<Void> startFuture) {
		
		clientTopics = HashMultimap.create(); // Configuramos el servidor MQTT
		
		MqttServer mqttServer = MqttServer.create(vertx);
		init(mqttServer);

		// Crear Cliente MQTT 1 que publica mensajes cada 3 segundos
		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
	
		//conexion al sevidor ( puerto 1883) - localhost -> IP Servidor
		
		mqttClient.connect(1883, "localhost", s -> {
			
			//subscripcion al topic2 - duda indicar QoS (AT_LEAST_ONCE)
			mqttClient.subscribe("topic_2", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					
					//Cliente subscrito al servidor - Mensaje de prueba
					System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal topic_2");
				}
			});

			
			//Timer -> simula envio de mensajes al servidor cada 3segs (duda segundo parametro 1000)
			new Timer().scheduleAtFixedRate(new TimerTask() {
				
				public void run() {
					// publicar mensaje en topic2 con el contenido "Ejemplo" y la hora
					// QoS-> al menos una vez / mensaje no es un duplicado(false) y no retenido por el canal (false)
					
					mqttClient.publish("topic_2",
							Buffer.buffer("Ejemplo a las " + Calendar.getInstance().getTime().toString()),
							MqttQoS.AT_LEAST_ONCE, false, false);}
			}, 1000, 3000);
		});
		
		// Crear cliente2 al que le deben llegar los mensajes del 1 (publicados en topic2) 
		//el mensaje llegara a cualquier cliente conectado al topic 2 (broker) 
		
		MqttClient mqttClient2 = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		
		mqttClient2.connect(1883, "localhost", s -> {

			//conexion al topic2 del cliente2
			mqttClient2.subscribe("topic_2", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) { 
					
					System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal topic_2");
					
					 //Registrar un manejador para interceptar los mensajes que llegan a nuestro cliente
					 /* Porceso: 
					 1.Cliente1 envia mensaje al servidor
					 2.Servidor lo recibe y busca los cliente subscritos al topic
					 3.Servidor reenvia mensaje a esos clientes
					 4.Los clientes reciben el mensaje y lo procesa si fuera necesario
					 */
					
					mqttClient2.publishHandler(new Handler<MqttPublishMessage>() {
						
						public void handle(MqttPublishMessage arg0) {
							
							//Si se publica este codigo el cliente2 ha recibido un mensaje
							System.out.println("Mensaje recibido por el cliente 2: " + arg0.payload().toString());
						}
					});
				}
			});
		});
	}

	
	private static void init(MqttServer mqttServer) { //Metodo para inicializar servidor y ajustar manejadores(funciones)
		mqttServer.endpointHandler(endpoint -> {
			
			//Se publica el sigu mensaje si un cliente se subscribe al servidor MQTT en algun topic
			System.out.println("Nuevo cliente MQTT [" + endpoint.clientIdentifier()
					+ "] solicitando suscribirse [Id de sesion: " + endpoint.isCleanSession() + "]");
			
			//Indicamos al cliente que se ha conectado al servidor y que no tenia sesion previamente creada (false)
			endpoint.accept(false);

			//Handler para gestionar las suscripciones a un topic. 
			//Aqui registraremos el cliente para poder reenviar todos los mensajes que se publicen en el topic al que se ha suscrito.
			handleSubscription(endpoint);

			//Handler para gestionar desuscripciones a un topic. 
			//Eliminar cliente de lista de clientes regs al topic para que no siga recibiendo mensajes del topic
			handleUnsubscription(endpoint);
			
			//Handler que sera llamado cuando se publique un mensaje en algun topic del servidor 
			//Funcion - Obtener todos clientes subscritos a un topic y reenviar mensaje a cada uno ( Broken Mqtt)
			//Broker sencillo para gestionar Qos, asegurar entrega, guardar mensaje en BD y despues entregarlos y guardar los clientes 
			//en caso de que cagia el servidor
			publishHandler(endpoint);

			//Handler para gestionar las desconexiones de los clientes al servidor.
			//Eliminar al cliente de los todos los topic a los que estuviera suscrito
			handleClientDisconnect(endpoint);
			
			
		}).listen(ar -> {
			if (ar.succeeded()) {
				System.out.println("MQTT server esta a la escucha por el puerto " + ar.result().actualPort());
			} else {
				System.out.println("Error desplegando el MQTT server");
				ar.cause().printStackTrace();
			}
		});
	}

	/** Metodo para gestionar suscripciones de los clientes a los topics @param endpoint */

	private static void handleSubscription(MqttEndpoint endpoint) {
		endpoint.subscribeHandler(subscribe -> {
			
			/* Los niveles de QoS -> tipo de entrega:
			- AT_LEAST_ONCE: Se asegura que los mensajes llegan a los clientes, pero no que se haga una unica vez (duplicados)
			- EXACTLY_ONCE: Se asegura que los mensajes llegan a los clientes una unica vez (+costoso)
			- AT_MOST_ONCE: No se asegura que el mensaje llegue al cliente (no necesario ACK)*/
			
			List<MqttQoS> grantedQosLevels = new ArrayList<>(); //DUDA para que sirve lista
			for (MqttTopicSubscription s : subscribe.topicSubscriptions()) { //lista Topics
				System.out.println("Suscripcion al topic " + s.topicName() + " con QoS " + s.qualityOfService());
				grantedQosLevels.add(s.qualityOfService());

				clientTopics.put(s.topicName(), endpoint); // Añadir cliente a lista de clientes suscritos al topic
			}
			
			//Enviar ACK al cliente de que se ha suscrito al topic y nivel Qos
			endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
		});
	}

	/** Metodo para eliminar la desuscripcion de un cliente a un topic. @param endpoint */
	
	private static void handleUnsubscription(MqttEndpoint endpoint) {
		endpoint.unsubscribeHandler(unsubscribe -> {
			
			for (String t : unsubscribe.topics()) {
				
				clientTopics.remove(t, endpoint); // Eliminar cliente de lista de clientes suscritos al topic
				System.out.println("Eliminada la suscripcion del topic " + t);
			}
			
			// Informamos al cliente que la desuscripcion se ha realizado
			endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
		});
	}
	

	/** Manejador encargado de notificar y procesar la desconexion de los clientes. @param endpoint */
	
	private static void handleClientDisconnect(MqttEndpoint endpoint) {
		endpoint.disconnectHandler(h -> {
			
			// Eliminamos al cliente de todos los topics a los que estaba suscritos
			Stream.of(clientTopics.keySet()).filter(e -> clientTopics.containsEntry(e, endpoint))
					.forEach(s -> clientTopics.remove(s, endpoint));
			
			System.out.println("El cliente remoto se ha desconectado [" + endpoint.clientIdentifier() + "]");
		});
	}

	
	/** Metodo encargado de interceptar y procesar los mensajes envidados por los clientes. @param endpoint */
	//DUDA
	private static void publishHandler(MqttEndpoint endpoint) {
		endpoint.publishHandler(message -> {
			
			//Suscribimos un handler cuando se solicidte publicaion de un mensaje en un topic
			handleMessage(message, endpoint);
			
		}).publishReleaseHandler(messageId -> {
			
			 //Suscribimos un handler cuando haya finalizado la publicacion del mensaje en el topic
			endpoint.publishComplete(messageId);
		});
	}
	

	/** Metodo para gestionar los mensajes salientes. @param message @param endpoint */
	
	private static void handleMessage(MqttPublishMessage message, MqttEndpoint endpoint) {
		
		System.out.println("Mensaje publicado por el cliente " + endpoint.clientIdentifier() + " en el topic " + message.topicName());
		System.out.println(" Contenido del mensaje: " + message.payload().toString());
		
		//Obtenemos todos los clientes suscritos al topic (excepto el que envia mensaje) 
		//para reenviar mensaje a todos (funcion broken MQTT)
		
		System.out.println("Origen: " + endpoint.clientIdentifier());
		
		for (MqttEndpoint client : clientTopics.get(message.topicName())) {
			System.out.println("Destino: " + client.clientIdentifier());
			if (!client.clientIdentifier().equals(endpoint.clientIdentifier())) //Si cliente no es el que manda mensaje mandar mensaje
				try {
					client.publish(message.topicName(), message.payload(), message.qosLevel(), message.isDup(),
							message.isRetain()).publishReleaseHandler(idHandler -> {
								client.publishComplete(idHandler);
							});
					
				} catch (Exception e) {
					System.out.println("Error, no se pudo enviar mensaje.");
				}
		}

		if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
			String topicName = message.topicName();
			switch (topicName) {
			
			//Se podria almacenar mensaje en algun registro de la base de datos
			
			}
			
			// Envia ACK al cliente de que el mensaje ha sido publicado
			endpoint.publishAcknowledge(message.messageId());
			
		} else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
			
			//Envia ACK al cliente de que mensaje ha llegao y cerrar canal para evitar mensajes duplicados (Qos)
			endpoint.publishRelease(message.messageId());
		}
	}

}