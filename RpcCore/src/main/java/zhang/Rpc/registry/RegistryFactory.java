package zhang.Rpc.registry;


import zhang.Rpc.spi.ExtensionLoader;


public class RegistryFactory {

    public static RegistryService get(String registryService) throws Exception {

        ExtensionLoader loader = ExtensionLoader.getInstance();

        return loader.get(registryService);
    }

    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(RegistryService.class);
    }

}
