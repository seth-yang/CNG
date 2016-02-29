命令总长度： 6
CMD_SET              S                      CMD_SET         TYPE     xx xx xx xx
CMD_RESET            R                      CMD_RESET       0        0  0  0  0
CMD_TOGGLE           T                      CMD_TOGGLE      TARGET   0  0  0  0
CMD_SEND_DATA        D                      CMD_SEND_DATA   TARGET   00 11 22 33
CMD_HELLO            H                      CMD_HELLO       0        0  0  0  0

// types
// 主要是针对 CMD_SET 命令的第二字节，
TYPE_DATA_TIMEOUT    D                      CMD_SET TYPE_DATA_TIMEOUT 00 00 00 64      // 设置数据采集间隔为 100s
TYPE_HELLO_TIMEOUT   H                      CMD_SET TYPE_HELLO_TIMEOUT 00 00 00 0A     // 设置心跳间隔为 10s
TYPE_IR_MODE         M                      CMD_SET TYPE_IR_MODE IR_MODE_LEARN 0 0 0   // 进入红外学习模式
                                            CMD_SET TYPE_IR_MODE IR_MODE_SILENCE 0 0 0 // 退出红外学习模式

// target
// 指明command的目标
/** 风机 */
TARGET_FAN           F                      CMD_TOGGLE TARGET_FAN 0 0 0 0              // 反转风机状态
                                            CMD_SEND_DATA TARGET_FAN 0 0 0 0           // 关闭风机
                                            CMD_SEND_DATA TARGET_FAN 0 0 0 1           // 打开风机
/** 红外人体感应器 */
TARGET_IR            I                      CMD_TOGGLE TARGET_IR 0 0 0 0               // 反转红外感应器状态
                                            CMD_SEND_DATA TARGET_IR 0 0 0 0            // 关闭红外感应器
                                            CMD_SEND_DATA TARGET_IR 0 0 0 1            // 打开红外感应器
/** 红外遥控器 */
TARGET_REMOTE        R                      CMD_SEND_DATA TARGET_IR 00 01 02 03        // 红外发送数据: 0x123
/** 磁力锁 */
TARGET_LOCK          L

// IR mode
IR_MODE_LEARN        L
IR_MODE_SILENCE      S