package zhang.Rpc.protocol.serialization;


import zhang.Rpc.spi.ExtensionLoader;

/**
 * @description: 序列化工厂
 */
public class SerializationFactory {


    public static RpcSerialization get(String serialization) throws Exception {

        return ExtensionLoader.getInstance().get(serialization);

    }

    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(RpcSerialization.class);
    }
}
