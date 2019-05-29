#include <Arduino.h>
#include <SPI.h>
#include <MFRC522.h>

#define SS_PIN D4 // Pin para el reset del RC522
#define RST_PIN D2 // Pin para el SS (SDA) del RC522


//Declaracion de cadena de caracteres

unsigned char data[16] = { '1', '2', '3', '2', '9', '1', '1', '2', '3', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
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

void setup() {
  Serial.begin(9600);
  SPI.begin();       // Init bus SPI
  mfrc522.PCD_Init(); // Init MFRC522
  Serial.println("RFID reading UID");

  for (byte i = 0; i < 6; i++) {
    key.keyByte[i] = 0xFF;
  }
}

void loop() {

  if (!mfrc522.PICC_IsNewCardPresent())
    return;

  if (!mfrc522.PICC_ReadCardSerial())
    return;

  MFRC522::StatusCode status;
  byte trailerBlock = 7;
  //byte sector = 1;
  byte blockAddr = 4;

  status = (MFRC522::StatusCode) mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, trailerBlock, &key, &(mfrc522.uid));
  if (status != MFRC522::STATUS_OK) {
    Serial.print(F("PCD_Authenticate() failed: "));
    Serial.println(mfrc522.GetStatusCodeName(status));
    return;
  }

  // Esbribir datos en block -----------------------------------------------------
/*
    Serial.print(F("Escribir datos en sector "));
    Serial.print(blockAddr);
    Serial.println(F(" ..."));
    printArray((byte*)data, 16);
    Serial.println();
    status = (MFRC522::StatusCode) mfrc522.MIFARE_Write(blockAddr, (byte*)data, 16);
    if (status != MFRC522::STATUS_OK) {
     Serial.print(F("MIFARE_Write() failed: "));
     Serial.println(mfrc522.GetStatusCodeName(status));
    }
    Serial.println();
*/

  byte buffer[18];
  byte size = sizeof(buffer);

  // Leer datos del block -----------------------------------------------------
  //Serial.print(F("Leer datos del sector ")); Serial.print(blockAddr);
  status = (MFRC522::StatusCode) mfrc522.MIFARE_Read(blockAddr, buffer, &size);
  if (status != MFRC522::STATUS_OK) {
    Serial.print(F("MIFARE_Read() failed: "));
    Serial.println(mfrc522.GetStatusCodeName(status));
  }

  //Serial.print(F("Data in block ")); Serial.print(blockAddr); Serial.println(F(":"));
  //printArray(buffer, 16); Serial.println();

  char datosNfc[16];

  for(int i = 0; i < 8; i++){
    datosNfc[i] = buffer[i];
  }

  // DATO:{ "IdProducto":12, "cantidad":3, "FechaCaducidad":29.11, "IdAlimento":2, "IdNevera" : 3 }

  String s1 = "{IdProducto: ";
  s1 += datosNfc[0];
  s1 += datosNfc[1] ;
  s1 += ", cantidad: ";
  s1 += datosNfc[2];
  s1 += ", FechaCaducidad: ";
  s1 += datosNfc[3];
  s1 += datosNfc[4];
  s1 += ",";
  s1 += datosNfc[5];
  s1 += datosNfc[6];

  s1 += ", IdAlimento: ";
  s1 += datosNfc[7];
  s1 += ", IdNevera: ";
  s1 += datosNfc[8];
  s1 += " }";

  Serial.print(s1);

  // Halt PICC
  mfrc522.PICC_HaltA();
  // Stop encryption on PCD
  mfrc522.PCD_StopCrypto1();
}
