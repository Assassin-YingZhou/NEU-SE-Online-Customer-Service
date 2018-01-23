/**
 * 在服务器上描述一个客户端连接
 *
 * @author 舒意恒
 * @see Message
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.concurrent.PriorityBlockingQueue;

@ServerEndpoint(value = "/ChatSocket")
public class ChatAnnotation {
    private Session session; // 与客户端通信的session
    private String nickname;
    private static ArrayList<Client> clients = new ArrayList<>(); // 在线客户
    // 覆盖比较器
    private static Comparator<Service> serviceComparator = Comparator.comparingInt(s -> s.getMyClients().size());
    private static PriorityBlockingQueue<Service> services = new PriorityBlockingQueue<>(5, serviceComparator); // <线程安全的优先级队列>在线客服
    private static Hashtable<String, ChatAnnotation> connections = new Hashtable<>();


    @OnOpen
    public void start(Session session) {
        this.session = session;
        this.nickname = session.getId();
        // 把当前客户端对应的ChatAnnotation对象加入到集合中
        connections.put(nickname, this);
    }

    @OnMessage
    public void incoming(String Msg) {
        Gson gson = new Gson();
        Message comingMsg = gson.fromJson(Msg, Message.class);
        int msgType = comingMsg.getType(); // 消息类型
        String content = comingMsg.getContent(); // 消息内容
        if (msgType == 0) // 上线消息
        {
            if (content.equals("0")) // 客户上线
            {
                Client newClient = new Client(nickname);
                clients.add(newClient); // 添加到客户列表
                Service yourService = services.peek();
                if (yourService == null)// 没有客服在线
                {
                    unicast("3", "", nickname, LocalDateTime.now(), Message.LOGIN);
                } else // 获得一个客服
                {
                    newClient.setMyService(yourService); // 设置该客户的客服
                    yourService.getMyClients().add(newClient); // 设置客服的客户列表
                    services.remove(yourService);
                    services.add(yourService);// 重新建堆
                    unicast("2", yourService.getNickname(), nickname, LocalDateTime.now(), Message.LOGIN);   // 向客户反馈客服信息，并告知客服有新客户
                }
            } else if (content.equals("1")) // 客服上线
            {
                Service newService = new Service(nickname);
                for (Client c : clients) {
                    if (c.getMyService() == null) // 如果有客户一直在等待客服上线
                    {
                        c.setMyService(newService);
                        newService.getMyClients().add(c);
                        unicast("2", newService.getNickname(), c.getNickname(), LocalDateTime.now(), Message.LOGIN);
                    }
                }
                services.add(newService);
            }
        } else if (msgType == 1) // 普通消息
        {
            for (Client c : clients) {
                if (c.getNickname().equals(nickname)) // 发送方是客户
                {
                    content = "客户 " + nickname + " ：" + content;
                }
            }
            for (Service s : services) {
                if (s.getNickname().equals(nickname)) // 发送方是客服
                {
                    content = "客服 " + nickname + " ：" + content;
                }
            }
            unicast(content, nickname, comingMsg.getReceiver(), LocalDateTime.now(), Message.NORMAL); // 服务器将消息推送给客户和客服的客户端
        }
    }

    @OnClose
    public void end() {
        for (Client c : clients) {
            if (c.getNickname().equals(nickname)) // 离线的是客户
            {
                if (c.getMyService() != null) {
                    c.getMyService().getMyClients().remove(c); // 删除其客服的客户列表中的一项
                    unicast("0", c.getNickname(), c.getMyService().getNickname(), LocalDateTime.now(), Message.QUIT);
                    services.remove(c.getMyService());
                    services.add(c.getMyService()); // 重新建堆
                }
                clients.remove(c);// 删除客户列表中的一项
                break;
            }
        }

        for (Service s : services) {
            if (s.getNickname().equals(nickname)) // 离线的是客服
            {
                services.remove(s);     // 删除客服列表中的一项
                if (s.getMyClients().size() != 0) {
                    for (Client client : s.getMyClients()) {
                        Service newService = services.peek();   // 为该客服的客户重新分配客服
                        if (newService == null)// 没有其他客服在线
                        {
                            client.setMyService(null);
                            unicast("3", s.getNickname(), client.getNickname(), LocalDateTime.now(), Message.QUIT);
                        } else // 获得一个新客服
                        {
                            client.setMyService(newService); // 设置该客户的客服
                            newService.getMyClients().add(client); // 设置客服的客户列表
                            services.remove(newService);
                            services.add(newService);// 重新建堆
                            unicast("2", newService.getNickname(), client.getNickname(), LocalDateTime.now(), Message.LOGIN);   // 告知客户已更换客服，告知新客服有新客户
                        }
                    }
                }
                break;
            }
        }
        connections.remove(nickname);  // 删除连接哈希表中的一项
    }


    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }


    private static void unicast(String msg, String sender, String receiver, LocalDateTime sendTime, int type) {
        Message Msg = new Message(msg, sender, receiver, sendTime, type);
        Gson gson = new GsonBuilder().create();
        String msgToJson = gson.toJson(Msg);
        ChatAnnotation recvChat = connections.get(receiver);
        ChatAnnotation sendChat = connections.get(sender);
        try {
            if (recvChat != null)
                synchronized (recvChat) {
                    recvChat.session.getBasicRemote().sendText(msgToJson); // 向接收方发送消息
                }
            if (sendChat != null && type != Message.QUIT)
                synchronized (sendChat) {
                    sendChat.session.getBasicRemote().sendText(msgToJson); // 发送方也需要收到自己发出的消息
                }
        } catch (IOException e) {
            connections.remove(recvChat);
            try {
                recvChat.session.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            // 向sender告知对方连接已中断
        }
    }
}