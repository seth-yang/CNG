#include <OneWire.h>
#include <DallasTemperature.h>
#include <dht11.h>

/**
 * DS18B20's port
 */
#define ONE_WIRE_BUS       2
/**
 * When the android connect me, turn this led on
 */
#define OK_BUS             3
/**
 * If the android is not connected, turn this led on
 */
#define ERROR_BUS          4
/**
 * The DHT11's port
 */
#define DHT11_BUS          5
/**
 * The MQ-Serial sensor's port
 */
#define MQ_BUS             A0
/*
#define TIMEOUT            1000
#define HELLO_TIMEOUT      1000
*/
#define MAX_LENGTH         5

/* The heartbeat command */
#define CMD_HELLO          'H'
/* Command to set something */
#define CMD_SET            'S'
/* Reset the SOC */
#define CMD_RESET          'R'

#define TYPE_DATA_TIMEOUT  'D'
#define TYPE_HELLO_TIMEOUT 'H'

/**
 * The access card's password
 */
/*#define PASSWORD           0x01030507 */

/**
 * The buffer of command what read from android
 * buff[0]   - the command
 * buff[1]   - if command == CMD_SET, this byte is non-zero
 * buff[2-3] - if command == CMD_set, there 2 bytes is the timeout value
 */
char buff[MAX_LENGTH];
long touch, hello_touch;
int  pos = 0, fail_count = 3;
int data_timeout = 1000, hello_timeout = 1000;

OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);
dht11 DHT11;

void setup() {
    Serial.begin (9600);
    pinMode (ERROR_BUS, OUTPUT);
    pinMode (OK_BUS, OUTPUT);

    digitalWrite (ERROR_BUS, HIGH);
    digitalWrite (OK_BUS, LOW);

    sensors.begin ();
    touch = hello_touch = millis ();
}

void loop() {
    readCommand ();

    long now = millis ();
    if (now - touch >= data_timeout) {
        sendData ();
    }

    if (now - hello_touch >= hello_timeout) {
        checkStatus ();
    }
}

/**
 * write the sensor's data to android.
 */
void sendData () {
    sensors.requestTemperatures ();
    float humidity = readHumidity ();
    int smoke = analogRead (MQ_BUS);
    Serial.print ("{\"D\":");
    Serial.print ("{\"T\":");
    Serial.print (sensors.getTempCByIndex (0));
    Serial.print (",\"H\":");
    Serial.print (humidity);
    Serial.print (",\"S\":");
    Serial.print (smoke);
    Serial.println ("}}");
    touch = millis ();
}

/**
 * convert 2 bytes to int
 */
int byteToInt (char *bytes, int start) {
    return (bytes [start] & 0xff) << 8 | (bytes [start + 1] & 0xff);
}

/**
 * read humidity from DHT11
 */
float readHumidity () {
    int chk = DHT11.read (DHT11_BUS);
    if (chk == DHTLIB_OK) {
        return (float) DHT11.humidity;
    }

    return -1.0f;
}

/**
 * check the heartbeat.
 */
void checkStatus () {
    if (fail_count > 0) {
        fail_count --;
        hello_touch = millis ();
    }
    if (fail_count <= 0) {
        digitalWrite (OK_BUS, LOW);
        digitalWrite (ERROR_BUS, HIGH);
    }
}

/**
 * read the command/setting/heartbeat from bluetooth adapter.
 */
void readCommand () {
    char c = Serial.read ();
    if (c >= 0) {
        // read a valid byte, append it to the buffer
        buff [pos ++] = c;
        if (pos >= MAX_LENGTH) {
            // reach the end of command
            // append tail
            buff [pos] = '\0';

            switch (buff [0]) {
                case CMD_HELLO :
                    fail_count = 3;
                    hello_touch = millis ();
                    digitalWrite (OK_BUS, HIGH);
                    digitalWrite (ERROR_BUS, LOW);
                    break;
                case CMD_RESET :
                    break;
                case CMD_SET : {
                    int n = byteToInt (buff, 2) * 1000;
                    if (buff [1] == TYPE_DATA_TIMEOUT)
                        data_timeout = n;
                    else if (buff [1] == TYPE_HELLO_TIMEOUT)
                        hello_timeout = n;
                    break;
                }
                default :
                    break;
            }

            memset (buff, 0, MAX_LENGTH);
            pos = 0;
        }
    }
}