#include <RFID.h>
/*
unsigned char header[16] = {
    0xca, 0xfe, 0x00, 0x01, 0x20, 0x16, 0x03, 0x08,   0x19, 0x07, 0x49, 0x00, 0x00, 0x00, 0xba, 0xbe
};
*/
//#define DEBUG

#define RST_PIN              5
#define SDA_PIN             10
#define BEEPER              A2

#define BUFF_SIZE           21
#define DATA_LENGTH         16

#define ACTION_READ        'R'
#define ACTION_WRITE       'W'

unsigned char DEFAULT_KEY[16] = {
        0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
        0xff, 0x07, 0x80, 0x69,
        0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
};
unsigned char APP_KEY[16] = {
        0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC,
        0xff, 0x07, 0x80, 0x69,
        0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
};

struct CNG_DATA {
    uint16_t      header;
    uint8_t       action;
    uint8_t       isAdmin;
    uint16_t      id;
    uint32_t      timestamp;
    uint32_t      expire;
    uint32_t      cardNo;
    uint8_t       crc;
    uint16_t      tailer;
};

union RemoteCommand {
    unsigned char buff[BUFF_SIZE];
    struct CNG_DATA data;
};

unsigned char RESET_DATA[16];
unsigned char SN[5];

RFID rfid(SDA_PIN, RST_PIN);
union RemoteCommand command;
int pos = 0;

void beep () {
    for (int i = 0; i < 100; i ++) {
        digitalWrite (BEEPER, HIGH);
        delayMicroseconds (200);
        digitalWrite (BEEPER, LOW);
        delayMicroseconds (200);
    }
    delay (200);
}

void error () {
    for (int i = 0; i < 3; i ++) {
        beep ();
    }
}

void setup () {
    Serial.begin(9600);
    SPI.begin();

    pinMode (BEEPER, OUTPUT);
    digitalWrite (BEEPER, LOW);

    memset (command.buff, 0, BUFF_SIZE);
}

inline void clear_command () {
    memset (command.buff, 0, BUFF_SIZE);
    pos = 0;
}

#ifdef DEBUG
void show_command () {
    Serial.println ("received command = {");
    Serial.print ("\t   Header : 0x");
    println (command.data.header);
    Serial.print ("\t   Action : 0x");
    println (command.data.action);
    Serial.print ("\t    Admin : ");
    Serial.println (command.data.isAdmin ? "true" : "false");
    Serial.print ("\t       ID : 0x");
    println (command.data.id);
    Serial.print ("\tTimestamp : 0x");
    println (command.data.timestamp);
    Serial.print ("\t   Expire : 0x");
    println (command.data.expire);
    Serial.print ("\t  Card No : 0x");
    println (command.data.cardNo);
    Serial.print ("\t      CRC : 0x");
    println (command.data.crc);
    Serial.print ("\t   Tailer : 0x");
    println (command.data.tailer);
}

void print (uint8_t n) {
    Serial.print ((n >> 4) & 0x0f, HEX);
    Serial.print (n & 0x0f, HEX);
}

void println (uint8_t n) {
    Serial.print ((n >> 4) & 0x0f, HEX);
    Serial.println (n & 0x0f, HEX);
}

void print (uint16_t n) {
    Serial.print ((n >> 12) & 0x0f, HEX);
    Serial.print ((n >>  8) & 0x0f, HEX);
    Serial.print (" ");
    Serial.print ((n >>  4) & 0x0f, HEX);
    Serial.print ( n        & 0x0f, HEX);
}

void println (uint16_t n) {
    print (n);
    Serial.println ();
}

void print (uint32_t n) {
    Serial.print ((n >> 28) & 0x0f, HEX);
    Serial.print ((n >> 24) & 0x0f, HEX);
    Serial.print (" ");
    Serial.print ((n >> 20) & 0x0f, HEX);
    Serial.print ((n >> 16) & 0x0f, HEX);
    Serial.print (" ");
    Serial.print ((n >> 12) & 0x0f, HEX);
    Serial.print ((n >>  8) & 0x0f, HEX);
    Serial.print (" ");
    Serial.print ((n >>  4) & 0x0f, HEX);
    Serial.print ( n        & 0x0f, HEX);
}

void println (uint32_t n) {
    print (n);
    Serial.println ();
}

void print (uint8_t *buff, int length) {
    for (int i = 0; i < length; i ++) {
        if (i > 0) Serial.print (" ");
        print (buff [i]);
    }
}

void println (uint8_t *buff, int length) {
    print (buff, length);
    Serial.println ();
}
#endif

void loop() {
    int ch = Serial.read ();
    if (ch >= 0) {
        command.buff [pos ++] = ch;
//        Serial.write (ch);
        if (pos >= BUFF_SIZE) {
#ifdef DEBUG
            Serial.print ("Receive data: ");
            println (command.buff, BUFF_SIZE);
#endif
            process_command ();
            clear_command ();
        }
    }
}

bool check_command () {
    if (command.data.header != 0xCAFE) {
#ifdef DEBUG
        Serial.println ("Invalid Header");
#endif
        return false;
    }

    if (command.data.tailer != 0xBABE) {
#ifdef DEBUG
        Serial.println ("Invalid Tailer");
#endif
        return false;
    }

    int sum = 0;
    for (int i = 2; i < 18; i ++) {
        sum += command.buff [i];
    }
    sum &= 0xff;
#ifdef DEBUG
    Serial.print ("sum = ");
    Serial.println (sum);
#endif
    if (sum != command.data.crc) {
#ifdef DEBUG
        Serial.print ("CRC Error. expect: ");
        Serial.print (sum);
        Serial.print (" but receive: ");
        println (command.data.crc);
#endif
        return false;
    }

    return true;
}

void process_command () {
    if (check_command ()) {
#ifdef DEBUG
        show_command ();
#endif
        switch (command.data.action) {
            case ACTION_READ :
                showSector (15, 2);
                break;
            case ACTION_WRITE :
#ifdef DEBUG
                Serial.println ("WRITE CARD");
#endif
                break;
            default :
#ifdef DEBUG
                Serial.print ("Action 0x");
                print (command.data.action);
                Serial.println (" is not supported.");
#endif
                break;
        }
    }
}

void showSector (int sector, int block) {
    rfid.init();
    rfid.isCard ();
    if (rfid.readCardSerial ()) {
        memcpy (SN, rfid.serNum, 5);
    }
    rfid.selectTag (SN);
    int offset = sector * 4 + 3;
    int state = rfid.auth (PICC_AUTHENT1A, offset, DEFAULT_KEY, SN);
    if (state == MI_OK) {
        beep ();
        offset = sector * 4 + block;
        unsigned char buff[16];
        state = rfid.read (offset, buff);
        if (state == MI_OK) {
            beep ();
            beep ();
            Serial.write (0xca);     // header 0
            Serial.write (0xfe);     // header 1
            Serial.write (1);        // state = success
            Serial.write ('R');      // type = read card
            Serial.write (16);       // length
            for (int i = 0; i < 16; i ++) {
                Serial.write (buff [i]);
            }

            int sum = 1 + 'R' + 16;
            for (int i = 0; i < 16; i ++) {
                sum += buff [i] & 0xff;
            }
            sum &= 0xff;
            Serial.write (sum);     // CRC
            Serial.write (0xba);  // tail
            Serial.write (0xbe);
        }
    }
}