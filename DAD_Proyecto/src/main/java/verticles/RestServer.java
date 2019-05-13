package verticles;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import entities.Sensor;
import entities.Usuario;
import entities.Alimento;
import entities.Temperatura;
import entities.Producto;
import entities.Nevera;
import entities.Historial;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestServer extends AbstractVerticle {

	private AsyncSQLClient mySQLClient;

	public void start(Future<Void> startFuture) {

		// configuracion inicializacion base de datos
		JsonObject config = new JsonObject().put("host", "localhost").put("username", "root").put("password", "root")
				.put("database", "dad_db").put("port", 3306);

		mySQLClient = MySQLClient.createShared(vertx, config); // crear conecxion
		Router router = Router.router(vertx); // Crear enrutador

		vertx.createHttpServer().requestHandler(router).listen(8089, result -> { // Servidor http para interceptar peticiones
			if (result.succeeded()) {
				System.out.println("Servidor desplegado");
			} else {
				System.out.println("Error de despliegue");
			}
		});

		router.route().handler(BodyHandler.create());

		// definir Get (se puede añadir expresion regular)
		// URL(base de datos del producto) /producto/metodo que gestiona la peticion
		// /: -> indica que es un parametro (no texto)
		
		//Sensores
		router.get("/sensores").handler(this::handleAllSensores);
		router.put("/sensores/add").handler(this::handleAñadirSensor);
		
		//Usuarios
		router.get("/usuarios").handler(this::handleAllUsuarios);
		router.put("/usuarios/add").handler(this::handleAñadirUsuario);
		router.put("/usuarios/eliminar").handler(this::handleEliminarUsuario); //eliminar por nombre y apellido
		
		//Alimentos
		router.get("/alimentos").handler(this::handleAllAlimentos);
		router.put("/alimentos/add").handler(this::handleAñadirAlimento);
		router.put("/alimentos/eliminar").handler(this::handleEliminarAlimento); //eliminar Alimento
				
		//Temperatura
		router.get("/temperaturas").handler(this::handleAllMediciones);
		router.put("/temperatura/add").handler(this::handleAñadirMedicion);
				
		//Productos
		router.get("/productos").handler(this::handleAllProductos);
		router.put("/productos/add").handler(this::handleAñadirProducto);
		router.put("/productos/eliminar").handler(this::handleEliminarProducto); //eliminar productro
		router.put("/productos/aumentar").handler(this::handleAumentarProducto); //sumar 1 de un producto
		router.put("/productos/disminuir").handler(this::handleDisminuirProducto); //restar 1 de un prodcuto
		router.put("/productos/fechaC").handler(this::handleFechaProducto); //actualizar FechaCaducidad producto
		
		//Historial
		router.get("/historial").handler(this::handleAllHistorial); 
		router.put("/historial/add").handler(this::handleAñadirHistorial); //Añadir alimento a historial de alimentos
		
		//Nevera
		router.get("/neveras").handler(this::handleAllNeveras);
		router.put("/neveras/add").handler(this::handleAñadirNevera);
		
		//ejemplos clase
		//router.put("/products/:productID/info").handler(this::handleProduct);
		//router.put("/products/:productID/:property").handler(this::handleProductProperty); 
	}
	
	/*

	private void handleProduct(RoutingContext routingContext) {

		String paramStr = routingContext.pathParam("productID"); // transformar a string product url
		int paramInt = Integer.parseInt(paramStr); // debe ser un productID si no no entra por enrutador
		JsonObject jsonObject = new JsonObject();

		jsonObject.put("serial", "asddasdasd");
		jsonObject.put("id", paramInt);
		jsonObject.put("name", "TV Samsumg");
		routingContext.response().putHeader("content-type", "application/json").end(jsonObject.encode());
	}

	private void handleProductProperty(RoutingContext routingContext) {

		String paramStr = routingContext.pathParam("productID"); // transformar a string product url
		int paramInt = Integer.parseInt(paramStr); // debe ser un productID si no no entra por enrutador
		JsonObject body = routingContext.getBodyAsJson();
		routingContext.response().putHeader("content-type", "application/json").end(body.encode());
	}
	
	*/

	private void handleAllSensores(RoutingContext routingContext) { 

		
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("SELECT * FROM sensor", result -> {
					if(result.succeeded()) {
						
						//Pasar de JsonObject a String
						Gson gson = new Gson();
						List<Sensor> Sensors = new ArrayList<>();
						for (JsonObject json : result.result().getRows()) {
							Sensors.add(gson.fromJson(json.encode(), Sensor.class));
						}
						routingContext.response().end(gson.toJson(Sensors));
						
						String jsonResult = new JsonArray (result.result().getRows()).encodePrettily();
						routingContext.response().end(jsonResult);   
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); // avisar a cliente de que ha petado la peticion
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}

	private void handleAllUsuarios(RoutingContext routingContext) { 
		
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("SELECT * FROM usuario ", result -> {
					if(result.succeeded()) {
						
						//Pasar de JsonObject a String
						Gson gson = new Gson();
						List<Usuario> Usuarios = new ArrayList<>();
						for (JsonObject json : result.result().getRows()) {
							Usuarios.add(gson.fromJson(json.encode(), Usuario.class));
						}
						routingContext.response().end(gson.toJson(Usuarios));
						
						String jsonResult = new JsonArray (result.result().getRows()).encodePrettily();
						routingContext.response().end(jsonResult);   
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); // avisar a cliente de que ha petado la peticion
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
	
    private void handleAñadirUsuario(RoutingContext routingContext) { 
 
		JsonObject jsonO = routingContext.getBodyAsJson();
    	//Integer IdUsuario = jsonO.getInteger("IdUsuario"); No hace falta porque la propiedad es AutoIncr
    	String name = jsonO.getString("name");
    	String surname = jsonO.getString("surname");
    	String contraseña = jsonO.getString("contraseña");
    		  			
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("INSERT INTO usuario (IdUsuario, name, surname, contraseña) "
						+ "VALUES (null, '" + name + "', '"+ surname +"', '" + contraseña + "')", result -> {
							
					if(result.succeeded()) {
							
						routingContext.response().setStatusCode(200).end();	
										
					}else { 
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleAñadirAlimento(RoutingContext routingContext) { 
     
        JsonObject jsonO = routingContext.getBodyAsJson();
        //Integer IdAlimento = jsonO.getInteger("IdAlimento");
        String nombre = jsonO.getString("Nombre");
        Integer temperaturaRecomendada = jsonO.getInteger("TemperaturaRecomendada");
                                     
        mySQLClient.getConnection(connection-> {
            if(connection.succeeded()) { 
 
                connection.result().query("INSERT INTO alimento (IdAlimento, Nombre, TemperaturaRecomendada) "
                        + "VALUES (null, '" + nombre + "', '"+ temperaturaRecomendada +"')", result -> {
                        
                    if(result.succeeded()) {
                        
                    	routingContext.response().setStatusCode(200).end();	                    
                        
                    }else { // aviso fallo
                        System.out.println(result.cause().getMessage());
                        routingContext.response().setStatusCode(400).end(); 
                    }
                });
                
            }else {
                System.out.println(connection.cause().getMessage());
                routingContext.response().setStatusCode(400).end(); 
            }
        });
    }
    
    private void handleAñadirSensor(RoutingContext routingContext) { 
    	
		JsonObject jsonO = routingContext.getBodyAsJson();
		//Integer IdSensor = jsonO.getInteger("IdSensor");
    	Integer IdNevera = jsonO.getInteger("IdNevera");
    	String Nombre = jsonO.getString("Nombre");
    		  			
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("INSERT INTO sensor (IdSensor, IdNevera, Nombre) VALUES (null, '" + IdNevera + "', '" + Nombre + "')", result -> {
					if(result.succeeded()) {
							
						routingContext.response().setStatusCode(200).end();	
					
					}else { // aviso fallo
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleAllAlimentos(RoutingContext routingContext) { 

		
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("SELECT * FROM alimento", result -> {
					if(result.succeeded()) {
						
						//Pasar de JsonObject a String
						Gson gson = new Gson();
						List<Alimento> alimentos = new ArrayList<>();
						for (JsonObject json : result.result().getRows()) {
							alimentos.add(gson.fromJson(json.encode(), Alimento.class));
						}
						routingContext.response().end(gson.toJson(alimentos));
						
						String jsonResult = new JsonArray (result.result().getRows()).encodePrettily();
						routingContext.response().end(jsonResult);   
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); // avisar a cliente de que ha petado la peticion
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleAñadirMedicion(RoutingContext routingContext) { 
    	
		JsonObject jsonO = routingContext.getBodyAsJson();
		Double FechaMedicion = jsonO.getDouble("FechaMedicion");
		Double valor = jsonO.getDouble("valor");
    	Integer IdSensor = jsonO.getInteger("IdSensor");
    		  			
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("INSERT INTO temperatura (FechaMedicion, valor, IdSensor) "
						+ "VALUES ('" + FechaMedicion + "', '" + valor + "', '" + IdSensor + "')", result -> {
					if(result.succeeded()) {
							
						routingContext.response().setStatusCode(200).end();	
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleAllMediciones(RoutingContext routingContext) { 

		
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("SELECT * FROM temperatura", result -> {
					if(result.succeeded()) {
						
						//Pasar de JsonObject a String
						Gson gson = new Gson();
						List<Temperatura> mediciones = new ArrayList<>();
						for (JsonObject json : result.result().getRows()) {
							mediciones.add(gson.fromJson(json.encode(), Temperatura.class));
						}
						routingContext.response().end(gson.toJson(mediciones));
						
						String jsonResult = new JsonArray (result.result().getRows()).encodePrettily();
						routingContext.response().end(jsonResult);   
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); // avisar a cliente de que ha petado la peticion
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleAñadirProducto(RoutingContext routingContext) { 
    	
		JsonObject jsonO = routingContext.getBodyAsJson();
		
		Integer IdProducto = jsonO.getInteger("IdProducto");
		Integer cantidad = jsonO.getInteger("cantidad");
		Double FechaCaducidad = jsonO.getDouble("FechaCaducidad");
    	Integer IdAlimento = jsonO.getInteger("IdAlimento");
    		  			
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("INSERT INTO producto (IdProducto, cantidad, FechaCaducidad, IdAlimento) "
						+ "VALUES ('" + IdProducto + "', '" + cantidad + "', '" + FechaCaducidad + "', '" + IdAlimento + "')", result -> {
					if(result.succeeded()) {
							
						routingContext.response().setStatusCode(200).end();	
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleAumentarProducto(RoutingContext routingContext) { 
    	
		JsonObject jsonO = routingContext.getBodyAsJson();
		Integer cantidad = jsonO.getInteger("cantidad");
		Integer IdAlimento = jsonO.getInteger("IdAlimento");
    		  			
		mySQLClient.getConnection(connection-> {
			
			if(connection.succeeded()) {
				connection.result().query("UPDATE producto SET cantidad = '" + (cantidad+1) + "' WHERE IdAlimento = '" + IdAlimento + "'", result -> {
					
					if(result.succeeded()) { 
						routingContext.response().setStatusCode(200).end();	
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
						
					}});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}});
	}
    
    private void handleDisminuirProducto(RoutingContext routingContext) { 
    	
		JsonObject jsonO = routingContext.getBodyAsJson();
		Integer cantidad = jsonO.getInteger("cantidad");
		Integer IdAlimento = jsonO.getInteger("IdAlimento");
    		  			
		mySQLClient.getConnection(connection-> {
			
			if(connection.succeeded()) {
				connection.result().query("UPDATE producto SET cantidad = '" + (cantidad-1) + "' WHERE IdAlimento = '" + IdAlimento + "'", result -> {
					
					if(result.succeeded()) { 
						routingContext.response().setStatusCode(200).end();	
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
						
					}});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}});
	}
    
    private void handleFechaProducto(RoutingContext routingContext) { 
    	
		JsonObject jsonO = routingContext.getBodyAsJson();
		Double FechaCaducidad = jsonO.getDouble("FechaCaducidad");
		Integer IdAlimento = jsonO.getInteger("IdAlimento");
    		  			
		mySQLClient.getConnection(connection-> {
			
			if(connection.succeeded()) {
				connection.result().query("UPDATE producto SET FechaCaducidad = '" + FechaCaducidad + "' WHERE IdAlimento = '" + IdAlimento + "'", result -> {
					
					if(result.succeeded()) { 
						routingContext.response().setStatusCode(200).end();	
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
						
					}});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}});
	}
    
    private void handleAllProductos(RoutingContext routingContext) { 
	
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("SELECT * FROM producto", result -> {
					if(result.succeeded()) {
						
						//Pasar de JsonObject a String
						Gson gson = new Gson();
						List<Producto> productos = new ArrayList<>();
						for (JsonObject json : result.result().getRows()) {
							productos.add(gson.fromJson(json.encode(), Producto.class));
						}
						routingContext.response().end(gson.toJson(productos));
						
						String jsonResult = new JsonArray (result.result().getRows()).encodePrettily();
						routingContext.response().end(jsonResult);   
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleEliminarUsuario(RoutingContext routingContext) { 
    	 
		JsonObject jsonO = routingContext.getBodyAsJson();
    	String name = jsonO.getString("name");
    	String surname = jsonO.getString("surname");
    			  			
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("DELETE FROM usuario WHERE name = '" + name + "' AND surname = '" + surname + "'", result -> {
					if(result.succeeded()) {
						
						routingContext.response().setStatusCode(200).end();	
						
					}else { 
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleEliminarAlimento(RoutingContext routingContext) { 
   	 
		JsonObject jsonO = routingContext.getBodyAsJson();
    	String Nombre = jsonO.getString("Nombre");
    			  			
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("DELETE FROM alimento WHERE Nombre = '" + Nombre + "'", result -> {
					if(result.succeeded()) {
						
						routingContext.response().setStatusCode(200).end();	
						
					}else { 
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleEliminarProducto(RoutingContext routingContext) { 
   	 
		JsonObject jsonO = routingContext.getBodyAsJson();
    	Integer IdAlimento = jsonO.getInteger("IdAlimento");
    			  			
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("DELETE FROM producto WHERE IdProducto = '" + IdAlimento + "' ", result -> {
					if(result.succeeded()) {
						
						routingContext.response().setStatusCode(200).end();	
						
					}else { 
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleAllHistorial(RoutingContext routingContext) { 
    	
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("SELECT * FROM historial", result -> {
					if(result.succeeded()) {
						
						//Pasar de JsonObject a String
						Gson gson = new Gson();
						List<Historial> historiales = new ArrayList<>();
						for (JsonObject json : result.result().getRows()) {
							historiales.add(gson.fromJson(json.encode(), Historial.class));
						}
						routingContext.response().end(gson.toJson(historiales));
						
						String jsonResult = new JsonArray (result.result().getRows()).encodePrettily();
						routingContext.response().end(jsonResult);   
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleAllNeveras(RoutingContext routingContext) { 
    	
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("SELECT * FROM nevera", result -> {
					if(result.succeeded()) {
						
						//Pasar de JsonObject a String
						Gson gson = new Gson();
						List<Nevera> neveras = new ArrayList<>();
						for (JsonObject json : result.result().getRows()) {
							neveras.add(gson.fromJson(json.encode(), Nevera.class));
						}
						routingContext.response().end(gson.toJson(neveras));
						
						String jsonResult = new JsonArray (result.result().getRows()).encodePrettily();
						routingContext.response().end(jsonResult);   
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleAñadirHistorial(RoutingContext routingContext) { 
    	
		JsonObject jsonO = routingContext.getBodyAsJson();
		
		Integer IdHistorial = jsonO.getInteger("IdHistorial");
		Integer IdAlimento = jsonO.getInteger("IdAlimento");
		String ultimaVez = jsonO.getString("ultimaVez");
    	
		mySQLClient.getConnection(connection-> {
			if(connection.succeeded()) {
				connection.result().query("INSERT INTO historial (IdHistorial, IdAlimento, ultimaVez) "
						+ "VALUES ('" + IdHistorial + "', '" + IdAlimento + "', '" + ultimaVez + "')", result -> {
					if(result.succeeded()) {
							
						routingContext.response().setStatusCode(200).end();	
						
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end(); 
					}
				});
				
			}else {
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();	
			}
		});
	}
    
    private void handleAñadirNevera(RoutingContext routingContext) { 
	
	JsonObject jsonO = routingContext.getBodyAsJson();
	
	Integer IdNevera = jsonO.getInteger("IdNevera");
	Integer IdUsuario = jsonO.getInteger("IdUsuario");
		  			
	mySQLClient.getConnection(connection-> {
		if(connection.succeeded()) {
			connection.result().query("INSERT INTO nevera (IdNevera, IdUsuario) "
					+ "VALUES ('" + IdNevera + "', '" + IdUsuario + "')", result -> {
				if(result.succeeded()) {
						
					routingContext.response().setStatusCode(200).end();	
					
				}else {
					System.out.println(result.cause().getMessage());
					routingContext.response().setStatusCode(400).end(); 
				}
			});
			
		}else {
			System.out.println(connection.cause().getMessage());
			routingContext.response().setStatusCode(400).end();	
		}
	});
}
}

