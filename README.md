com.yangc.bridge
===============

### 基于Mina的初级推送服务器实现
### 1.result
每个request的发生都要返回对应的response。<br />
协议：<br />
0x68 [contentType(0x00)] [uuid] [toLength] [dataLength] [to] 0x68 [success] [message] [crc] 0x16

### 2.login
在首次消息发送前，必须先登录，如果多次验证失败，将强制关闭连接；如果绕开登录直接发送消息，也将强制关闭连接。登录成功后，会收到之前未读的离线文本，或者未收到的离线文件。<br />
协议：<br />
0x68 [contentType(0x01)] [uuid] 0x68 [usernameLength] [passwordLength] [username] [password] [crc] 0x16

### 3.chat
发送文本，如果接收方没有在线，将转为离线文本，待下次登录时，会接收到。<br />
协议：<br />
0x68 [contentType(0x02)] [uuid] [fromLength] [toLength] [dataLength] [from] [to] 0x68 [data] [crc] 0x16

### 4.ready_file
发送文件之前要询问对方是否接收此文件，同时会携带要传输的文件的属性信息。如果接收方返回接收，则开始发送文件；如果接收方返回拒绝，则不发送文件。<br />
协议：<br />
0x68 [contentType(0x03)] [uuid] [fromLength] [toLength] [from] [to] 0x68 [fileNameLength] [fileName] [fileSize] [crc] 0x16

### 5.transmit_file
发送文件，如果接收方没有在线，将转为离线文件，待下次登录时，会询问是否接收离线文件。<br />
协议：<br />
0x68 [contentType(0x04)] [uuid] [fromLength] [toLength] [dataLength] [from] [to] 0x68 [fileNameLength] [fileName] [fileSize] [fileMd5] [offset] [data] [crc] 0x16

### 6.heart
发送心跳，保证连接可用。<br />
协议：<br />
0x68 [contentType(0x55)] 0x68 [crc] 0x16

### Tips：
    1.这里说的消息包括：文本、文件。
    2.是否支持离线文本，可以配置。
    3.支持离线文件，但是不支持断点续传。
    4.支持黑名单，白名单的设置。
