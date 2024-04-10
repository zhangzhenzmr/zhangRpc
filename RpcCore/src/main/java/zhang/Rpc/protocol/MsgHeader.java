package zhang.Rpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 消息头
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MsgHeader implements Serializable {
    private short magic; // 魔数
    private byte version; // 协议版本号
    private byte msgType; // 消息的类型
    private byte status; // 状态
    private long requestId; // 消息 ID
    private int serializationLen;
    private byte[] serializations;
    private int msgLen; // 数据长度
}
