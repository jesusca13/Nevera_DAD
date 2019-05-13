package verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class jsonExamples extends AbstractVerticle {
	
	public void start(Future<Void> startFuture) {
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.put("elem1", 123);
		jsonObject.put("elem2", "Object");
		jsonObject.put("elem3", "Object");
		jsonObject.put("elem4", "Object");
		jsonObject.put("elem5", "Object");
		jsonObject.put("elem6", "Object");
		jsonObject.put("elem7", "Object");
		System.out.println(jsonObject.encodePrettily()); // formatear json con saltos de linea , etc
		System.out.println(jsonObject.encode());
		
		JsonObject decode = new JsonObject(jsonObject.encode());
		System.out.println(decode.getInteger("elem1"));
		System.out.println(decode.getString("elem2"));
		System.out.println(decode.getDouble("elem1"));
		
	}
	
	/*
	//Creacion objetos json
	//String jsonString = "{\"foo\":\"bar\"}";
	//JsonObject object1 = new JsonObject(jsonString);
	
	//insertar valores
	JsonObject object = new JsonObject();
	object.put("foo", "bar").put("num", 123).put("myBool", true);
	
	//obtener valores
	String val = object.getString("some-Key");  
	int intval = object.getInteger("some-other-key");
	
	//crear jsonArrays
	//String jsonString = "{\"foo\":\"bar\"}";
	//JsonArray array = new JsonArray(jsonString);
	
	//insertar valores
	JsonArray array = new JsonArray();
	array.add("foo").add(123).add(false);
	
	//Obtener valores
	String valArray = array.getString(0);
	Integer intvalArray = array.getInteger(1);
	Boolean boolVal = array.getBoolean(2);
		*/

}
