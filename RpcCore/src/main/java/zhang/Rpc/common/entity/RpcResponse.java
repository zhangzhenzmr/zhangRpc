package zhang.Rpc.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 *  RPC 调用结果的封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse implements Serializable {

    private Object data;
    private Class dataClass;
    private String message;
    private Exception exception;

}
