#include <IOXhop_FirebaseESP32.h>
#include <SimpleTimer.h>
#include <DHTesp.h>

// Se define la red wifi y la contraseña
#define WIFI_SSID "Ivan Zhen"
#define WIFI_PASSWORD "Ivan Zhen"

#define TEMPERATURE1_PATH "/devices/0x00000001/temperature1/value"
#define HUMIDITY1_PATH "/devices/0x00000001/humidity1/value"
#define GAS1_PATH "/devices/0x00000001/gas1/risk"
#define LIGHT_PATH "/devices/0x00000001/rele1/state"
#define PLUG_PATH "/devices/0x00000001/rele2/state"

// Se define el Timer y cuantos 'hilos' ejecutara el programa
SimpleTimer t;
int id_timer_gas1,id_timer_temperature1, id_timer_humidity1, id_timer_light, id_timer_plug;

StreamHandlerCallback stream;

// DHT Variables
const int DHT_pin = 16;  // Pin del sensor DHT
DHTesp dht;  // Se instancia el sensor DHT
static char celsiusTemp[7];
static char humidityTemp[7];

// MQ9 Variables
const int MQ9_pin = 35;

// Reles Variables
const int light_pin = 26;
const int plug_pin = 25;

// Se definen las variables del programa
bool initialized = false;
int gas1_risk;
String light_state;
String plug_state;
float temperature1_value;
float humidity1_value;

void setup() {
  Serial.begin(115200);

  initializeWifi();  // Se inicializa el Wifi
  
  initializeFirebase();  // Se inicializa la monitorizacion de Firebase

  dht.setup(DHT_pin, DHTesp::DHT22);  // Se inicializa el sensor DHT
  pinMode(MQ9_pin, INPUT); // Se establece el pin de MQ9 como entrada de datos
  pinMode(light_pin, OUTPUT);  // Se inicializa el rele de la luz
  pinMode(plug_pin, OUTPUT);  // Se inicializa el rele del enchufe
}

void loop() {
 t.run();
}

void updateGas() {
  // Lectura del sensor de gas
  int gas1_value = analogRead(MQ9_pin);

  if (gas1_value >= 0 && gas1_value < 1365) {
    gas1_risk = 0;  // Se establece un riesgo bajo para la salud
  }
  else if (gas1_value >= 1365 && gas1_value < 2730) {
    gas1_risk = 1;  // Se establece un riesgo medio para la salud
  }
  else {
    gas1_risk = 2;  // Se establece un riesgo alto para la salud
  }

  // Actualizamos el riesgo de gas en Firebase
  Serial.println("Updated Gas Risk: " + String(gas1_risk));
  Firebase.set(GAS1_PATH, gas1_risk);
}

void updateTemperature() {
  // Lectura de la temperatura y humedad
  TempAndHumidity newValues = dht.getTempAndHumidity();
  
  if (dht.getStatus() != 0) {
    Serial.println("DHT22 error status: " + String(dht.getStatusString()));
  }

  temperature1_value = dht.computeHeatIndex(newValues.temperature, newValues.humidity);

  // Actualizamos la temperatura en Firebase
  Serial.println("Updated Temperature Value: " + String(temperature1_value));
  Firebase.set(TEMPERATURE1_PATH, temperature1_value);
}

void updateHumidity() {
  // Lectura de la temperatura y humedad
  TempAndHumidity newValues = dht.getTempAndHumidity();
  
  if (dht.getStatus() != 0) {
    Serial.println("DHT22 error status: " + String(dht.getStatusString()));
  }
  
  humidity1_value = dht.computeDewPoint(newValues.temperature, newValues.humidity);

  // Actualizamos la humedad en Firebase
  Serial.println("Updated Humidity Value: " + String(humidity1_value));
  Firebase.set(HUMIDITY1_PATH, humidity1_value);
}

void updateLight() {
  if (light_state == "on") {
    digitalWrite(light_pin, LOW);
  }
  else if (light_state == "off") {
    digitalWrite(light_pin, HIGH);
  }
}

void updatePlug() {
  if (plug_state == "on") {
    digitalWrite(plug_pin, LOW);
  }
  else if (plug_state == "off") {
    digitalWrite(plug_pin, HIGH);
  }
}

void initializeTimers() {
  id_timer_gas1 = t.setInterval(5000, updateGas);
  id_timer_temperature1 = t.setInterval(30000, updateTemperature);
  id_timer_humidity1 = t.setInterval(30000, updateHumidity);
  id_timer_light = t.setInterval(1000, updateLight);
  id_timer_plug = t.setInterval(1000, updatePlug);

  t.setInterval(5000, reconnectWifi);  // Se monitoriza el estado de la conexion wifi
}

void stopTimers() {
  t.disable(id_timer_gas1);
  t.disable(id_timer_temperature1);
  t.disable(id_timer_humidity1);
  t.disable(id_timer_light);
  t.disable(id_timer_plug);
}

void startTimers() {
  t.enable(id_timer_gas1);
  t.enable(id_timer_temperature1);
  t.enable(id_timer_humidity1);
  t.enable(id_timer_light);
  t.enable(id_timer_plug);
}

void initializeWifi() {
  WiFi.setAutoReconnect(true);  // Establecemos que si se pueda usar la funcion de reconnectar si se pierde la conexion
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);  // Inicializamos los datos de la conexion
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {  // Si no hay conexion
    Serial.print(".");
    WiFi.waitForConnectResult();  // Esperamos a que se obtenga un resultado de la conexion
    delay(3500);
    if (WiFi.status() != WL_CONNECTED) {  // Si sigue sin haber conexion
      WiFi.reconnect();  // Intentamos reconectarnos
    }
  }
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());
}

void reconnectWifi() {
  if (WiFi.status() != WL_CONNECTED) {  // Si no hay conexion
    Serial.println("reconnecting...");
    WiFi.reconnect();  // Intentamos reconectarnos
    WiFi.waitForConnectResult();  // Esperamos a que se obtenga un resultado de la conexion
  }
}

void initializeFirebaseStream() {
  stream = [](FirebaseStream stream) {
    String eventType = stream.getEvent();
    eventType.toLowerCase();
    
    Serial.print("event: ");
    Serial.println(eventType);
    if (eventType == "put") {

      // Se inicializan los componentes en el primer GET que se hace a Firebase
      if (!initialized) {
        JsonObject& root = stream.getData();  // Se obtiene un JSON con todos los datos
        initializeComponents(root);  // Se inicializan los componentes a partir del JSON
      }

      else {
        String path = stream.getPath();  // Se obtiene la ruta donde se ha producido la modificacion
  
        if (path == "/gas1/risk") {  // Se obtiene el nuevo valor para el Gas Risk
          //gas1_risk = stream.getDataInt();  // El valor del gas lo actualiza el ESP32, por lo que se comenta esta linea para que no haya redundancia
          Serial.println("Gas Risk: " + String(gas1_risk));
        }
        else if (path == "/rele1/state") {  // Se obtiene el nuevo valor para el Rele1 State
          light_state = stream.getDataString();
          updateLight();
          Serial.println("Light State: " + light_state);
        }
        else if (path == "/rele2/state") {  // Se obtiene el nuevo valor para el Rele2 State
          plug_state = stream.getDataString();
          updatePlug();
          Serial.println("Plug State: " + plug_state);
        }
        else if (path == "/temperature1/value") {  // Se obtiene el nuevo valor para el Temperature Value
          //temperature1_value = stream.getDataFloat();  // El valor de la temperatura lo actualiza el ESP32, por lo que se comenta esta linea para que no haya redundancia
          Serial.println("Temperature Value: " + String(temperature1_value));
        }
        else if (path == "/humidity1/value") {  // Se obtiene el nuevo valor para el Humidity Value
          //humidity1_value = stream.getDataFloat();  // El valor de la humedad lo actualiza el ESP32, por lo que se comenta esta linea para que no haya redundancia
          Serial.println("Humidity Value: " + String(humidity1_value));
        }
      }
    }
  };
}

void initializeFirebase() {
  Firebase.begin("futh-dam.firebaseio.com", "2PnufNyb7nG6jTgbhZ9a61xPxMLTjffJra7oNyIj");
  initializeFirebaseStream();  // Se inicializa el valor del stream
  Firebase.stream("/devices/0x00000001", stream);  // Se inicia el stream de Firebase
  initializeTimers();  // Se inicializan los timers despues de conectarse con Firebase
}

void initializeComponents(JsonObject& root) {
  if (root.containsKey("gas1")) {  // Se inicializa gas1
    gas1_risk = root["gas1"]["risk"].as<int>();
    Serial.println("Initial Gas Risk: " + String(gas1_risk));
  }
  if (root.containsKey("rele1")) {  // Se inicializa rele1
    light_state = root["rele1"]["state"].as<String>();
    Serial.println("Initial Light State: " + light_state);
  }
  if (root.containsKey("rele2")) {  // Se inicializa rele2
    plug_state = root["rele2"]["state"].as<String>();
    Serial.println("Initial Plug State: " + plug_state);
  }
  if (root.containsKey("temperature1")) {  // Se inicializa temperature1
    temperature1_value = root["temperature1"]["value"].as<float>();
    Serial.println("Initial Temperature Value: " + String(temperature1_value));
  }
  if (root.containsKey("humidity1")) {  // Se inicializa humidity1
    humidity1_value = root["humidity1"]["value"].as<float>();
    Serial.println("Initial Humidity Value: " + String(humidity1_value));
  }
  
  initialized = true;
}
