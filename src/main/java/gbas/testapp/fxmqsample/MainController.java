package gbas.testapp.fxmqsample;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {

    private Scene scene;

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * message to sent field
     */
    public TextArea fSendMessage;

    /**
     * label for sent message
     */
    public Label lSendMessage;

    /**
     * sned message button
     */
    public Button bSendMessage;

    /**
     * received message field
     */
    public TextArea fReceiveMessage;

    /**
     * label for received message
     */
    public Label lReceivedMessage;

    /**
     * mq queue manager
     */
    public TextField fMqQueueManager;

    /**
     * mq channel
     */
    public TextField fMqChannel;

    /**
     * mq connection host and port
     */
    public TextField fMqConnName;

    /**
     * mq user
     */
    public TextField fMqUser;

    /**
     * mq password
     */
    public TextField fMqPassword;

    /**
     * mq request queue name
     */
    public TextField fMqRequestQueue;

    /**
     * mq response queue name
     */
    public TextField fMqResponseQueue;

    /**
     * exit button
     */
    public Button bExit;

    /**
     * save settings button
     */
    public Button bSaveSettings;

    @FXML
    public void initialize() {
        Settings settings = Settings.load();

        if (settings.getMqQueueManager() != null) {
            fMqQueueManager.setText(settings.getMqQueueManager());
        }
        if (settings.getMqChannel() != null) {
            fMqChannel.setText(settings.getMqChannel());
        }
        if (settings.getMqConnName() != null) {
            fMqConnName.setText(settings.getMqConnName());
        }
        if (settings.getMqUsername() != null) {
            fMqUser.setText(settings.getMqUsername());
        }
        if (settings.getMqPassword() != null) {
            fMqPassword.setText(settings.getMqPassword());
        }
        if (settings.getMqRequestQueue() != null) {
            fMqRequestQueue.setText(settings.getMqRequestQueue());
        }
        if (settings.getMqResponseQueue() != null) {
            fMqResponseQueue.setText(settings.getMqResponseQueue());
        }
    }

    /**
     * send message button action
     *
     * @param actionEvent
     */
    public void sendMessage(ActionEvent actionEvent) {
        scene.setCursor(Cursor.WAIT);
        bSendMessage.setDisable(true);

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() {
                String correlationId = null;
                try {
                    correlationId = sendMessage(fMqRequestQueue.getText().trim());
                } catch (JMSException e) {
                    Platform.runLater(() -> lSendMessage.setText(e.getMessage().length() > 50 ? (e.getMessage().substring(0, 47) + "...") : e.getMessage()));
                    e.printStackTrace();
                }

                if (correlationId != null) {
                    Platform.runLater(() -> {
                        lReceivedMessage.setText("");
                        fReceiveMessage.setText("");
                    });

                    TextMessage message = null;

                    String finalCorrelationId = correlationId;
                    Platform.runLater(() -> lSendMessage.setText("JmsCorrelationId: " + finalCorrelationId));

                    try {
                        bSendMessage.setDisable(true);
                        message = receiveMessage(correlationId);
                    } catch (JMSException e) {
                        Platform.runLater(() -> lReceivedMessage.setText(substring(e.getMessage(), 50)));
                    } finally {
                        if (message != null) {
                            TextMessage finalMessage = message;
                            Platform.runLater(() -> {
                                try {
                                    fReceiveMessage.setText(finalMessage.getText());
                                    lReceivedMessage.setText("JmsMessageId: " + finalMessage.getJMSMessageID());
                                } catch (JMSException e) {
                                    e.printStackTrace();
                                }
                            });
                        }

                    }
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            bSendMessage.setDisable(false);
            scene.setCursor(Cursor.DEFAULT);
        });
        new Thread(task).start();
    }

    private TextMessage receiveMessage(String correlationId) throws JMSException {
        MQConnectionFactory mqConnectionFactory = createMqConnectionFactory();

        String user = fMqUser.getText().trim();
        String password = fMqPassword.getText().trim();

        TextMessage receivedMessage = null;

        try (Connection connection = mqConnectionFactory.createConnection(user, password);
             Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE)) {

            connection.start();

            Queue mqsrc = session.createQueue(fMqResponseQueue.getText().trim());

            try (MessageConsumer srcQ = session.createConsumer(mqsrc, "JMSCorrelationID='" + correlationId + "'")) {
                Message message = srcQ.receive(TimeUnit.MINUTES.toMillis(5));
                if (message instanceof TextMessage) {
                    receivedMessage= (TextMessage) message;
                }
            }

            session.commit();

            connection.stop();

            mqConnectionFactory.clear();
        }

        return receivedMessage;
    }

    private String substring(String s, int length) {
        return s.length() > length ? (s.substring(0, length - 3) + "...") : s;
    }

    public void actionClose(ActionEvent actionEvent) {
        final Node source = (Node) actionEvent.getSource();
        final Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * send mq message
     * @return
     * @throws JMSException
     */
    private String sendMessage(String message) throws JMSException {

        String correlationId = UUID.randomUUID().toString();

        MQConnectionFactory mqConnectionFactory = createMqConnectionFactory();

        String user = fMqUser.getText().trim();
        String password = fMqPassword.getText().trim();

        try (Connection connection = mqConnectionFactory.createConnection(user, password);
             Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE)) {

            connection.start();

            Queue mqdst = session.createQueue(message);

            try (MessageProducer dstQ = session.createProducer(mqdst)) {
                Message msg = session.createTextMessage(fSendMessage.getText());

                msg.setJMSCorrelationID(correlationId);

                dstQ.send(msg);
            }

            session.commit();

            connection.stop();

            mqConnectionFactory.clear();
        }

        return correlationId;
    }

    /**
     * create mq connection factory
     * @return
     * @throws JMSException
     */
    private MQConnectionFactory createMqConnectionFactory() throws JMSException {
        MQConnectionFactory mqConnectionFactory = new MQConnectionFactory();

        String queueManager = fMqQueueManager.getText().trim();
        String channel = fMqChannel.getText().trim();
        String connName = fMqConnName.getText().trim();

        String host = null, port = null;
        // convert connName to HOST and PORT
        Matcher m = Pattern.compile("(.*)\\((\\d+)\\)").matcher(connName);
        if (m.find()) {
            host = m.group(1);
            port = m.group(2);
        }

        mqConnectionFactory.setHostName(host);
        mqConnectionFactory.setQueueManager(queueManager);
        mqConnectionFactory.setPort(Integer.parseInt(port));
        mqConnectionFactory.setChannel(channel);
        mqConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        mqConnectionFactory.setCCSID(1208);

        return mqConnectionFactory;
    }

    /**
     * save settings button action
     *
     * @param actionEvent
     */
    public void saveSettings(ActionEvent actionEvent) {
        Settings settings = new Settings();

        settings.setMqQueueManager(fMqQueueManager.getText().trim());
        settings.setMqChannel(fMqChannel.getText().trim());
        settings.setMqConnName(fMqConnName.getText().trim());
        settings.setMqUsername(fMqUser.getText().trim());
        settings.setMqPassword(fMqPassword.getText().trim());
        settings.setMqRequestQueue(fMqRequestQueue.getText().trim());
        settings.setMqResponseQueue(fMqResponseQueue.getText().trim());

        settings.save();
    }
}
