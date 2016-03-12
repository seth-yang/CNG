#ifndef _lib_card_h_
#define _lib_card_h_

#include <RFID.h>

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


/*######################################### declare ########################## */
void showSector (int sector, int block);
inline void clear_command (RemoteCommand *command, int *pos);
inline void beep ();
inline void error ();
inline bool check_command (RemoteCommand *command);

/*########################## implementations ############################ */
inline void clear_command (RemoteCommand *command, int *pos) {
    memset (command->buff, 0, BUFF_SIZE);
    *pos = 0;
}

inline void beep () {
    for (int i = 0; i < 100; i ++) {
        digitalWrite (BEEPER, HIGH);
        delayMicroseconds (200);
        digitalWrite (BEEPER, LOW);
        delayMicroseconds (200);
    }
    delay (200);
}

inline void error () {
    for (int i = 0; i < 3; i ++) {
        beep ();
    }
}

inline bool check_command (RemoteCommand *command) {
    if (command->data.header != 0xCAFE) {
        return false;
    }

    if (command->data.tailer != 0xBABE) {
        return false;
    }

    int sum = 0;
    for (int i = 2; i < 18; i ++) {
        sum += command->buff [i];
    }
    sum &= 0xff;
    return sum == command->data.crc;

}

void process_command () {
    if (check_command (&command)) {
        switch (command.data.action) {
            case ACTION_READ :
                showSector (15, 2);
                break;
            case ACTION_WRITE :
                break;
            default :
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
#endif