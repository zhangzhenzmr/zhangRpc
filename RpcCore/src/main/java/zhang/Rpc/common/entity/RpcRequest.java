package zhang.Rpc.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @description: 请求体
 */
@Data
public class RpcRequest implements Serializable {

    private String serviceVersion;
    private String className;
    private String methodName;
    private Object data;
    private Class dataClass;
    private Class<?>[] parameterTypes;
    private Map<String,Object> serviceAttachments;
    private Map<String,Object> clientAttachments;

}
