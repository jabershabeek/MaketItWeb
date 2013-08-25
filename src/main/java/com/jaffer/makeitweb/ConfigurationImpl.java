package com.jaffer.makeitweb;

public class ConfigurationImpl extends Configuration {

    private String host = "localhost";
    private String port = "8080";
    private String args = "";
    private String vmargs = "";
    private String main[]=new String[10];
    private int clients = 10;

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getArgs() {
        return args;
    }

    public String getVmargs() {
        return vmargs;
    }

    public String getMain(int i) {
        return main[i];
    }

    public int getClients() {
        return clients;
    }

    protected void setClients(int clients) {
        this.clients = clients;
    }

    protected void setHost(String host) {
        this.host = host;
    }

    protected void setPort(String port) {
        this.port = port;
    }

    protected void setArgs(String args) {
        this.args = args;
    }

    protected void setVmargs(String vmargs) {
        this.vmargs = vmargs;
    }

    protected void setMain(int i,String main) {
        this.main[i] = main;
    }

}
