package verticles;

import java.util.Random;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;

public class TcpClient extends AbstractVerticle{
	
	public void start(Future<Void> startFuture) {
		
		NetClientOptions netClientOptions = new NetClientOptions();
		netClientOptions.setConnectTimeout(10000).setReconnectAttempts(5).setReconnectInterval(1000);
		
		NetClient netClient = vertx.createNetClient(netClientOptions);
		netClient.connect(8086, "localHost/ 127.0.0.1", connection -> {
			
			JsonObject resquest = new JsonObject();
			Random random = new Random();
			
			connection.result().handler(message-> {
				System.out.println(message.toJsonObject().encodePrettily());
			});
			
			switch (random.nextInt(3)) {
			
			case 0:
				resquest.put("req", 1);
				break;
				
			case 1:
				resquest.put("req", 2);
				
					switch (random.nextInt(3)) {
					case 0:
						resquest.put("content", "temperatura");
						break;
					case 2:
						resquest.put("content", "humidity");
						break;
					default:
						resquest.put("content", "pressure");
						break;
					}
				
				break;
				
			default:
				resquest.put("req", 3);
				break;
			}
			
			connection.result().write(resquest.encode());
			
		});
		
	}

}
