package com.jaffer.makeitweb.server;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ScheduledFuture;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment.Variable;
import org.webswing.Constants;

import com.corundumstudio.socketio.SocketIOClient;
import com.jaffer.makeitweb.Configuration;

public class SwingJvmConnection implements MessageListener, Runnable {

    private Session session;
    private MessageProducer producer;
    private MessageConsumer consumer;
    private SocketIOClient client;
    private String clientId;
    private ScheduledFuture<?> exitSchedule;

    public SwingJvmConnection(String clientId, Connection c, SocketIOClient client) {
        this.client = client;
        this.clientId = clientId;
        try {
            session = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue producerQueue = session.createQueue(clientId + Constants.SERVER2SWING);
            Queue consumerQueue = session.createQueue(clientId + Constants.SWING2SERVER);
            consumer = session.createConsumer(consumerQueue);
            consumer.setMessageListener(this);
            producer = session.createProducer(producerQueue);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void send(Serializable o) {
        try {
            producer.send(session.createObjectMessage(o));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String s) {
        try {
            producer.send(session.createTextMessage(s));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void sendKill() {
        try {
            producer.send(session.createTextMessage(Constants.SWING_KILL_SIGNAL));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void onMessage(Message m) {
        try {
            if (m instanceof ObjectMessage) {
                client.sendJsonObject(((ObjectMessage) m).getObject());
            } else if (m instanceof TextMessage) {
                String text = ((TextMessage) m).getText();
                if (text.equals(Constants.SWING_SHUTDOWN_NOTIFICATION)) {
                    consumer.close();
                    producer.close();
                    session.close();
                    System.out.println("notifying browser shutdown");
                    client.sendMessage(Constants.SWING_SHUTDOWN_NOTIFICATION);
                    SwingServer.removeSwingClientApplication(clientId);
                    client.disconnect();
                    if(this.getExitSchedule()!=null){
                        this.getExitSchedule().cancel(false);
                    }
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public void start() {
        new Thread(new Runnable() {

            public void run() {
                Project project = new Project();
                project.setBaseDir(new File(System.getProperty("user.dir")));
                project.init();
                DefaultLogger logger = new DefaultLogger();
                project.addBuildListener(logger);
                logger.setOutputPrintStream(System.out);
                logger.setErrorPrintStream(System.err);
                logger.setMessageOutputLevel(Project.MSG_INFO);
                //System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
                //System.setErr(new PrintStream(new DemuxOutputStream(project, true)));
                project.fireBuildStarted();
                Throwable caught = null;
                try {
                    Java javaTask = new Java();
                    javaTask.setTaskName(clientId);
                    javaTask.setProject(project);
                    javaTask.setFork(true);
                    javaTask.setFailonerror(true);
                    javaTask.setJar(new File("makeitwebswingmatcher.jar"));
                    javaTask.setArgs(Configuration.getInstance().getArgs());
                    javaTask.setJvmargs("-noverify " + Configuration.getInstance().getVmargs());

                    Variable clientIdVar = new Variable();
                    clientIdVar.setKey(Constants.SWING_START_SYS_PROP_CLIENT_ID);
                    clientIdVar.setValue(clientId);
                    javaTask.addSysproperty(clientIdVar);

                    Variable mainClass = new Variable();
                    mainClass.setKey(Constants.SWING_START_SYS_PROP_MAIN_CLASS);
                    mainClass.setValue(Configuration.getInstance().getMain(Configuration.mainToSelect));
                    javaTask.addSysproperty(mainClass);

                    javaTask.init();
                    javaTask.executeJava();
                } catch (BuildException e) {
                    e.printStackTrace();
                    caught = e;
                }
                project.fireBuildFinished(caught);
            }
        }).start();
    }

    public SocketIOClient getClient() {
        return client;
    }

    public void setClient(SocketIOClient client) {
        this.client = client;
    }

    public String getClientId() {
        return clientId;
    }

    public void run() {
        sendKill();
    }

    public void setExitTimer(ScheduledFuture<?> schedule) {
        this.exitSchedule = schedule;
    }

    protected ScheduledFuture<?> getExitSchedule() {
        return exitSchedule;
    }

    protected void setExitSchedule(ScheduledFuture<?> exitSchedule) {
        this.exitSchedule = exitSchedule;
    }

}
