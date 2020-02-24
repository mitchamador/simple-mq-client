package gbas.testapp.fxmqsample;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Settings {

    private static final String APPLICATION_PROPERTIES = "application.properties";

    private String mqQueueManager;

    private String mqChannel;

    private String mqConnName;

    private String mqUsername;

    private String mqPassword;

    private String mqRequestQueue;

    private String mqResponseQueue;

    public String getMqQueueManager() {
        return mqQueueManager;
    }

    public void setMqQueueManager(String mqQueueManager) {
        this.mqQueueManager = mqQueueManager;
    }

    public String getMqChannel() {
        return mqChannel;
    }

    public void setMqChannel(String mqChannel) {
        this.mqChannel = mqChannel;
    }

    public String getMqConnName() {
        return mqConnName;
    }

    public void setMqConnName(String mqConnName) {
        this.mqConnName = mqConnName;
    }

    public String getMqUsername() {
        return mqUsername;
    }

    public void setMqUsername(String mqUsername) {
        this.mqUsername = mqUsername;
    }

    public String getMqPassword() {
        return mqPassword;
    }

    public void setMqPassword(String mqPassword) {
        this.mqPassword = mqPassword;
    }

    public String getMqRequestQueue() {
        return mqRequestQueue;
    }

    public void setMqRequestQueue(String mqRequestQueue) {
        this.mqRequestQueue = mqRequestQueue;
    }

    public String getMqResponseQueue() {
        return mqResponseQueue;
    }

    public void setMqResponseQueue(String mqResponseQueue) {
        this.mqResponseQueue = mqResponseQueue;
    }

    public Settings() {

    }

    public static Settings load() {
        Settings settings = new Settings();

        try {
            Properties properties = new Properties();
            Path propFile = Paths.get(APPLICATION_PROPERTIES);
            properties.loadFromXML(Files.newInputStream(propFile));

            settings.mqQueueManager = properties.getProperty("mqQueueManager");
            settings.mqChannel = properties.getProperty("mqChannel");
            settings.mqConnName = properties.getProperty("mqConnName");
            settings.mqUsername = properties.getProperty("mqUsername");
            settings.mqPassword = properties.getProperty("mqPassword");
            settings.mqRequestQueue = properties.getProperty("mqRequestQueue");
            settings.mqResponseQueue = properties.getProperty("mqResponseQueue");

        } catch (IOException e) {
            System.out.println("no " + APPLICATION_PROPERTIES);
            //e.printStackTrace();
        }

        return settings;
    }

    public void save() {
        try {
            Properties properties = new Properties();

            properties.put("mqQueueManager", this.mqQueueManager);
            properties.put("mqChannel", this.mqChannel);
            properties.put("mqConnName", this.mqConnName);
            properties.put("mqUsername", this.mqUsername);
            properties.put("mqPassword", this.mqPassword);
            properties.put("mqRequestQueue", this.mqRequestQueue);
            properties.put("mqResponseQueue", this.mqResponseQueue);

            Path propFile = Paths.get(APPLICATION_PROPERTIES);

            try (OutputStream outputStream = Files.newOutputStream(propFile)) {
                properties.storeToXML(outputStream, "Application settings");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
