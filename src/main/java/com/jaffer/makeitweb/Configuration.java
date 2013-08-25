package com.jaffer.makeitweb;

public abstract class Configuration {

    private static Configuration singleton = new ConfigurationImpl();
 
    public static int mainToSelect=0;
    
    public abstract String getHost();

    public abstract String getPort();

    public abstract String getArgs();

    public abstract String getVmargs();

    public abstract String getMain(int i);

    public abstract int getClients();

    public static Configuration getInstance() {
        return singleton;
    }

}
