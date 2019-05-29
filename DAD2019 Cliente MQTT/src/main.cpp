#include <Arduino.h>
#include <ESP8266Wifi.h> //gestion wifi
#include <PubSubClient.h> //gestion mqtt
#include <ESP8266HTTPClient.h> //gestion placa
#include <ArduinoJson.h> // gestion objetos json
#include <Adafruit_Sensor.h>
#include <DHT.h>
#include <DHT_U.h>
#include <SPI.h> //nfc
#include <MFRC522.h> //nfc

const char* ssid = "vodafone5B58"; //"Wifiplus_Montano";
const char* password = "DKJMMVGMYUT4AD"; //"4815162342";
const char* channel_name = "topic_2";
const char* mqtt_server = "192.168.0.155";
const char* http_server = "192.168.0.155";
const char* http_server_port = "8089";
String clientId;

WiFiClient espClient; //para hacer peticiones put/get
PubSubClient client(espClient);
long lastMsg = 0;
long lastMsgRest = 0; //llamamos a la apiRest cada 3 segs
char msg[50];
int value = 0; // guardar httpCode de la peticion devuelve apiResT


//Pines para sensor nfc
#define SS_PIN D4 // Pin para el reset del RC522
#define RST_PIN D2 // Pin para el SS (SDA) del RC522

// DATO:{ "IdProducto":3, "cantidad":4, "FechaCaducidad":29.11, "IdAlimento":2 }

unsigned char data[16] = { '3', '4', '2', '9', '1', '1', '2', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}; //Contenido tarjeta nfc
unsigned char *writeData = data;
unsigned char *str;

MFRC522 mfrc522(SS_PIN, RST_PIN);  // Crear instancia del MFRC522
MFRC522::MIFARE_Key key;

void printArray(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? " 0" : " ");
    Serial.print(buffer[i], HEX);
  }
}


void setup_wifi() {

  // Conexion a la red WiFi
  delay(10); //damos 10segs para arranque placa
  randomSeed(micros()); //Semilla para generar nº aleatorios de IdClients

  Serial.println();
  Serial.print("Conectando a la red WiFi ");
  Serial.println(ssid);

  WiFi.begin(ssid, password); //conexion a la wifi

  // Mientras que no estemos conectados a la red, seguimos leyendo el estado
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  // En este punto el ESP se encontrara registro en la red WiFi indicada, por
  // lo que es posible obtener su direccion IP
  Serial.println("");
  Serial.println("WiFi conectado");
  Serial.println("Direccion IP registrada: ");
  Serial.println(WiFi.localIP());
}

void callback(char* topic, byte* payload, unsigned int length) {

  /* Metodo llamado por el cliente MQTT cuando se recibe un mensaje en un canal
  al que se encuentra suscrito.
  Como actuar en funcion a unos valores de entrada
  parametros(canal(topic), mensaje, tamaña mensaje)
  */
  Serial.print("Mensaje recibido [canal: ");
  Serial.print(topic);
  Serial.print("] ");

  // Leemos la informacion del cuerpo del mensaje.
  //Para ello necesitamos el puntero al mensaje y su tamaño
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]); //imprimir cuerpo del mensaje mqtt
  }
  Serial.println();

  //Procesar json
  DynamicJsonDocument doc(length);
  deserializeJson(doc, payload, length);
  const char* action = doc["action"];
  Serial.printf("Accion %s\n", action);

    //si recibe un on/off del cliente mqtt la placa encendera/apagara led
  // Encendemos un posible switch digital (un diodo led por ejemplo) si el
  // contenido del cuerpo es 'on'
  if (strcmp(action, "on") == 0) {
    digitalWrite(BUILTIN_LED, HIGH);
    Serial.println("Detectada accion de activacion");
  } else if (strcmp(action, "off") == 0) {
    digitalWrite(BUILTIN_LED, LOW);
    Serial.println("Detectada accion de desactivacion");
  } else{
    Serial.println("Accion no reconocida");
  }
}

void reconnect() {
  //Reconexion al servidor Mqtt y suscripcion al canal
  // Esperamos a que el cliente se conecte al servidor
  // Tambien se fija el identificador del cliente
  while (!client.connected()) {
    Serial.print("Conectando al servidor MQTT...");
    // Crer Idcliente aleatorio. Cuidado, esto debe
    // estar previamente definido en un entorno real, ya que debemos
    // identificar al cliente de manera univoca en la mayori­a de las ocasiones
    clientId = "ESP8266Client-";
    clientId += String(random(0xffff), HEX);
    // Intentamos la conexiÃ³n del cliente
    if (client.connect(clientId.c_str())) { //todoOK
      String printLine = "   Cliente " + clientId + " conectado al servidor" + mqtt_server;
      Serial.println(printLine);
      // Publicamos un mensaje en el canal indicando que el cliente se ha
      // conectado. Esto avisarÃ­a al resto de clientes que hay un nuevo
      // dispositivo conectado al canal. Puede ser interesante en algunos casos.
      String body = "Dispositivo con ID = ";
      body += clientId;
      body += " conectado al canal ";
      body += channel_name;
      client.publish(channel_name, "");//publicar
      //Se muestra en consola del servidorMqtt
      client.subscribe(channel_name); //Suscribimos el cliente al canal.

    } else { //Error
      Serial.print("Error al conectar al canal, rc=");
      Serial.print(client.state());
      Serial.println(". Intentando de nuevo en 5 segundos.");
      delay(5000);
    }
  }
}

void makeGetRequest(){
    HTTPClient http;
    // Abrimos la conexion con el servidor REST y definimos la URL del recurso
    String url = "http://";
    url += http_server; //Ip pc ( donde este servidor)
    url += ":";
    url += http_server_port;
    url += "/productos";

    String message = "Enviando peticion GET (PRODUCTOS) al servidor REST. ";
    message += url;
    Serial.println(message);
    http.begin(url);

    int httpCode = http.GET(); // codigo de estado de la respuesta

    if (httpCode > 0){
    //Si httpCode > 0 , tenemos repuesta , aunque puede ser negativa ( cod 400)
    // Obtenemos el cuerpo de la respuesta y lo imprimimos por el puerto serie
     String payload = http.getString();
     Serial.println("payload: " + payload);

     const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
     DynamicJsonDocument root(bufferSize);
     deserializeJson(root, payload);

     const char* serial = root["serial"];
     const char* id = root["id"];
     const char* name = root["name"];

     Serial.print("Name:");
     Serial.println(name);
     Serial.print("Serial:");
     Serial.println(serial);
     Serial.print("Id:");
     Serial.println(id);
    }

    Serial.printf("\nRespuesta servidor REST %d\n", httpCode);
    // Cerramos la conexion con el servidor REST
    http.end();
}

void makePutRequestProducto(char datosNfc[16]){

    HTTPClient http;
    // Abrimos la conexion con el servidor REST y definimos la URL del recurso
    String url = "http://";
    url += http_server;
    url += ":";
    url += http_server_port;
    url += "/productos/add";

    String message = "Enviando peticion PUT de un producto al servidor REST. ";
    message += url;
    Serial.println(message);
    // Realizamos la peticion y obtenemos el codigo de estado de la respuesta
    http.begin(url);

    const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
    DynamicJsonDocument root(bufferSize);

    String IdProducto = "";
    IdProducto += datosNfc[0];
    IdProducto += datosNfc[1];

    String fecha = "";
    fecha += datosNfc[3];
    fecha += datosNfc[4];
    fecha += ",";
    fecha += datosNfc[5];
    fecha += datosNfc[6];

    root["IdProducto"] = atoi(IdProducto);
    root["cantidad"] = atoi(datosNfc[2]);
    root["FechaCaducidad"] = atoi(fecha);
    root["IdAlimento"] = 1;
    root["IdNevera"] = 3;

    String json_string;
    serializeJson(root, json_string);

    Serial.print(json_string);

    int httpCode = http.PUT(json_string); // codigo respuesta

    if (httpCode > 0){
     // Si el codigo devuelto es > 0, significa que tenemos respuesta, aunque
     // no necesariamente va a ser positivo (podrÃ­a ser un codigo 400).
     // Obtenemos el cuerpo de la respuesta y lo imprimimos por el puerto serie

     String payload = http.getString();
     Serial.println("payload put: " + payload);
    }

    Serial.printf("\nRespuesta servidor REST PUT %d\n", httpCode);
    // Cerramos la conexion con el servidor REST
    http.end();
}


void setup() {

  //------------------------------------------------- MQTT

  Serial.begin(9600);
  setup_wifi(); // Conexion a red WiFi
  client.setServer(mqtt_server, 1883); // Direccion/puerto servidor MQTT

  client.setCallback(callback); //EJC cada vez que se publique un mensaje en canal comun

  //------------------------------------------------- NFC

  SPI.begin();       // Init bus SPI
  mfrc522.PCD_Init(); // Init MFRC522
  Serial.println("RFID reading UID");

  for (byte i = 0; i < 6; i++) {
    key.keyByte[i] = 0xFF;
  }

  //-------------------------------------------------- DHP11

}



void loop() {

  if (!client.connected()) {
    reconnect();
  }

  client.loop(); // Esperamos que algun cliente suscrito al canal publique algo

  //Enviar mensj cada 15 seg sin bloquear loop
  long now = millis();

  if (now - lastMsg > 15000) {
    lastMsg = now;
    ++value;

  // Construimos JSON con el contenido del mensaje a publicar
  StaticJsonDocument<200> doc;
  doc["clientId"] = clientId;
  doc["message"] = "periodic message";
  doc["number"] = value;
  String output;
  serializeJson(doc, output);
  Serial.print("Mensaje publicado: ");
  Serial.println(output);
  client.publish(channel_name, output.c_str());
}

/* if (now - lastMsgRest > 15000) {
    lastMsgRest = now;
    makeGetRequest();
    makePutRequest();
*/


//----------------------------------------------------------------------------

if (!mfrc522.PICC_IsNewCardPresent())
  return;

if (!mfrc522.PICC_ReadCardSerial())
  return;

MFRC522::StatusCode status;
byte trailerBlock = 7;
byte blockAddr = 4;

status = (MFRC522::StatusCode) mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, trailerBlock, &key, &(mfrc522.uid));
if (status != MFRC522::STATUS_OK) {
  Serial.print(F("PCD_Authenticate() failed: "));
  Serial.println(mfrc522.GetStatusCodeName(status));
  return;
}

byte buffer[18];
byte size = sizeof(buffer);

// Leer datos del block

status = (MFRC522::StatusCode) mfrc522.MIFARE_Read(blockAddr, buffer, &size);
if (status != MFRC522::STATUS_OK) {
  Serial.print(F("MIFARE_Read() failed: "));
  Serial.println(mfrc522.GetStatusCodeName(status));
}

char datosNfc[16];

for(int i = 0; i < 8; i++){
  datosNfc[i] = buffer[i];
}


// Halt PICC
mfrc522.PICC_HaltA();
// Stop encryption on PCD
mfrc522.PCD_StopCrypto1();

// poner buffer[0] - 48;

Serial.print(datosNfc);
String s1 = "Enviando Datos : {IdProducto: ";
s1 += datosNfc[0]; s1 += datosNfc[1] ; s1 += ", cantidad: "; s1 += datosNfc[2];
s1 += ", FechaCaducidad: "; s1 += datosNfc[3]; s1 += datosNfc[4]; s1 += ",";
s1 += datosNfc[5]; s1 += datosNfc[6]; s1 += ", IdAlimento: ";
s1 += datosNfc[7]; s1 += ", IdNevera: ";
s1 += datosNfc[8]; s1 += " }";

Serial.print(s1);
makePutRequestProducto(datosNfc);




}
