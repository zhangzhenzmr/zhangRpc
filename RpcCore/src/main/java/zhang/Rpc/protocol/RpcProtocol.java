package zhang.Rpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcProtocol<T> implements Serializable {
    private MsgHeader header;
    private T body;

}
