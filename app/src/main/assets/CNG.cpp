#include <OneWire.h>
#include <DallasTemperature.h>
#include <dht11.h>

#define ONE_WIRE_BUS  2
#define LED           3
#define ERROR_BUS     4
#define DHT11_BUS     5

#define TIMEOUT       1000
#define HELLO_TIMEOUT 1000
#define MAX_LENGTH    5

char buff[MAX_LENGTH + 1];
char HELLO[6] = {'H', 'E', 'L', 'L', 'O', '\0'};
long touch, hello_touch;
int  pos = 0, fail_count = 3;
OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);
dht11 DHT11;

void setup() {
    Serial.begin (9600);
    pinMode (ERROR_BUS, OUTPUT);
    pinMode (LED, OUTPUT);

    digitalWrite (ERROR_BUS, HIGH);
    digitalWrite (LED, LOW);

    sensors.begin ();
    touch = hello_touch = millis ();
}

void loop() {
    char c = Serial.read ();
    if (c > 0) {
        buff [pos ++] = c;
        if (pos >= MAX_LENGTH) {
            buff [pos] = '\0';

            if (strcmp (buff, HELLO) == 0) {
                fail_count = 3;
                hello_touch = millis ();
                digitalWrite (LED, HIGH);
                digitalWrite (ERROR_BUS, LOW);
            }

            memset (buff, 0, MAX_LENGTH);
            pos = 0;
        }
    }

    long now = millis ();
    if (now - touch >= TIMEOUT) {
        sensors.requestTemperatures ();
        float humidity = readHumidity ();
        Serial.println (String ("{\"T\":") + sensors.getTempCByIndex (0) + ",\"H\":" + humidity + "}");
        touch = now;
    }

    if (now - hello_touch >= HELLO_TIMEOUT) {
        if (fail_count > 0) {
            fail_count --;
            hello_touch = now;
        }
        if (fail_count <= 0) {
            digitalWrite (LED, LOW);
            digitalWrite (ERROR_BUS, HIGH);
        }
    }
}

float readHumidity () {
    int chk = DHT11.read (DHT11_BUS);
    if (chk == DHTLIB_OK) {
        return (float) DHT11.humidity;
    }

    return -1.0f;
}