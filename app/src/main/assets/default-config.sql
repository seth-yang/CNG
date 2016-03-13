-- system setup
INSERT INTO _conf VALUES ('app.version', '版本', '1.00', 'Text', 'false', 'true');
INSERT INTO _conf VALUES ('url.cloud', '云服务器', 'http://10.247.1.8:8080/cng/', 'Uri', 'true', 'true');
INSERT INTO _conf VALUES ('gather.interval', '数据采集间隔(秒)', '1', 'Integer', 'true', 'true');
INSERT INTO _conf VALUES ('hello.interval', '心跳包时间间隔(秒)', '10', 'Integer', 'true', 'true');
INSERT INTO _conf VALUES ('upload.interval', '数据提交间隔(秒)', '60', 'Integer', 'true', 'true');
INSERT INTO _conf VALUES ('upload.buff.size', '数据提交缓存大小(条)', '60', 'Integer', 'true', 'true');
-- default ir code
INSERT INTO _ir_code (_name, _chinese, _code) VALUES ('air.control.open',  '打开空调', null);
INSERT INTO _ir_code (_name, _chinese, _code) VALUES ('air.control.close', '关闭空调', null);

-- for debug
INSERT INTO _card (_card_no) values (11549);
INSERT INTO _card (_card_no) values (123456);
INSERT INTO _conf VALUES ('card.major.version', '主版本号', '1', 'Integer', 'true', 'true');
INSERT INTO _conf VALUES ('card.minor.version', '次版本号', '0', 'Integer', 'true', 'true');