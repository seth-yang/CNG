#include <OneWire.h>
#include <DallasTemperature.h>
#include <dht11.h>
#include <IRremote.h>

/**
 * DS18B20's port
 */

#define ONE_WIRE_BUS       2
#define IR_LED             3             // REQUIRED.
/**
 * When the android connect me, turn this led on
 */
#define OK_BUS            A4
/**
 * If the android is not connected, turn this led on
 */
#define ERROR_BUS         A5
/**
 * The DHT11's port
 */
#define DHT11_BUS          5

#define RECEIVER_BUS       6

#define FAN_BUS            7
#define LIGHT_BUS          8
#define LOCK_BUS           9
#define DOOR_SENSOR_BUS   10

/**
 * The MQ-Serial sensor's port
 */
#define MQ_BUS             A0
/*
#define TIMEOUT            1000
#define HELLO_TIMEOUT      1000
*/
#define MAX_LENGTH         6


/* The heartbeat command */
#define CMD_HELLO          'H'
/* Command to set something */
#define CMD_SET            'S'
/* Reset the SOC */
#define CMD_RESET          'R'
#define CMD_SEND_DATA      'D'

#define TYPE_DATA_TIMEOUT  'D'
#define TYPE_HELLO_TIMEOUT 'H'
#define TYPE_MODE          'M'

#define MODE_LEARN         'L'
#define MODE_SILENT        'S'

#define TARGET_REMOTE      'R'
#define TARGET_FAN         'F'
#define TARGET_LOCK        'L'
#define TARGET_LIGHT       'T'

OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensor(&oneWire);
dht11 DHT11;
IRrecv receiver(RECEIVER_BUS);
IRsend sender;
decode_results results;

long    touch,                              // sensor data touch timestamp
        hello_touch,                        // hello touch timestamp
        data_timeout = 1000,                // sensor data timeout
        hello_timeout = 1000;               // hello timeout
int pos = 0,                                // command read position
        fail_count = 3,                         // bluetooth connect fail count
        mode = MODE_SILENT,                     // IR Control mode
        door_value, v1, mismatch_count = 0;     // door sensor variables
char command[MAX_LENGTH];

/**
 * Read commands from Serial Port
 */
void readCommand () {
    while (Serial.available ()) {
        char ch = Serial.read ();
        if (ch >= 0) {
            command [pos ++] = ch;
        }
    }

    if (pos >= MAX_LENGTH) {
        char  cmd  = command [0];
        char  type = command [1];
        char *buff = command + 2;

        process (cmd, type, buff);
        memset (command, 0, MAX_LENGTH);
        pos = 0;
    }
}

/**
 * Send data to bluetooth
 */
void sendSensorData (long now) {
    if (now - touch > data_timeout) {
        sensor.requestTemperatures ();
        float humidity = readHumidity ();
        int smoke = analogRead (MQ_BUS);
        Serial.print ("{\"D\":");
        Serial.print ("{\"T\":");
        Serial.print (sensor.getTempCByIndex (0));
        Serial.print (",\"H\":");
        Serial.print (humidity);
        Serial.print (",\"S\":");
        Serial.print (smoke);
        Serial.println ("}}");
        touch = millis ();
    }
}

/**
 * Convert 4 bytes to an integer
 */
int byteToInt (const char *bytes, int start) {
    return (bytes [start    ] & 0xff) << 24 |
           (bytes [start + 1] & 0xff) << 16 |
           (bytes [start + 2] & 0xff) << 8  |
           (bytes [start + 3] & 0xff);
}

float readHumidity () {
    int chk = DHT11.read (DHT11_BUS);
    if (chk == DHTLIB_OK) {
        return (float) DHT11.humidity;
    }

    return -1.0f;
}

void checkStatus (long now) {
    if (now - hello_touch > hello_timeout) {
        if (fail_count > 0) {
            fail_count--;
            hello_touch = millis ();
        }

        if (fail_count <= 0) {
            digitalWrite (OK_BUS, LOW);
            digitalWrite (ERROR_BUS, HIGH);
        }
    }
}

void process (const char cmd, const char type, const char *buff) {
    switch (cmd) {
        case CMD_HELLO : {
            fail_count = 3;
            hello_touch = millis ();
            digitalWrite (OK_BUS, HIGH);
            digitalWrite (ERROR_BUS, LOW);
            break;
        }
        case CMD_RESET :
            ;
            break;
        case CMD_SET : {
            int n = byteToInt (buff, 0) * 1000;
            set (type, n);
            break;
        }
        case CMD_SEND_DATA : {
            int data = byteToInt (buff, 0) * 1000;
            sendData (type, data);
            break;
        }
        default :
            break;
    }
}

void set (char target, int value) {
    switch (target) {
        case TYPE_DATA_TIMEOUT :
            data_timeout = value;
            break;
        case TYPE_HELLO_TIMEOUT :
            hello_timeout = value;
            break;
        case TYPE_MODE :
            mode = value;
            break;
        default :
            break;
    }
}

void sendData (char target, int value) {
    switch (target) {
        case TARGET_FAN :
            digitalWrite (FAN_BUS, value);
            break;
        case TARGET_LOCK :
            digitalWrite (LOCK_BUS, value);
            break;
        case TARGET_REMOTE :
            sender.sendNEC (value);
            break;
        case TARGET_LIGHT :
            digitalWrite (LIGHT_BUS, value);
            break;
        default :
            break;
    }
}

void doWork () {
    long now = millis ();
    checkStatus (now);
    sendSensorData (now);
    learn ();
    checkEvent ();
}

void learn () {
    if (mode == MODE_LEARN) {
        if (receiver.decode (&results)) {
            Serial.print ("{\"C\":{\"C\":");
            Serial.print (results.value);
            Serial.println ("}}");
        }
    }
}

void checkEvent () {
    int value = digitalRead (DOOR_SENSOR_BUS);
    if (value != door_value) {
        if (mismatch_count >= 5) {
            if (value == HIGH) {
                door_value = 1;
                Serial.println ("{\"E\":{\"D\":\"C\"}}");
                // door is closed.
            } else {
                door_value = 0;
                Serial.println ("{\"E\":{\"D\":\"O\"}}");
                // door is opened.
            }
            v1 = door_value;
            mismatch_count = 0;
        } else {
            mismatch_count++;
        }
    }
}

void setup () {
    Serial.begin (9600);

    pinMode (ERROR_BUS, OUTPUT);
    pinMode (OK_BUS, OUTPUT);
    pinMode (IR_LED, OUTPUT);
    pinMode (DOOR_SENSOR_BUS, INPUT_PULLUP);

    digitalWrite (ERROR_BUS, HIGH);
    digitalWrite (OK_BUS, LOW);

    sensor.begin ();
    touch = hello_touch = millis ();
    door_value = digitalRead (DOOR_SENSOR_BUS);
}

void loop () {
    readCommand ();
    doWork ();
}