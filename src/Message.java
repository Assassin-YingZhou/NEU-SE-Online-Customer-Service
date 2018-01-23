/**
 * 描述客户端与服务器之间的消息
 *
 * @author 舒意恒
 * @see ChatAnnotation
 */

/*
      *** Message 约定 ***

      ChatAnnotation类的unicast方法不仅会将消息发送给receiver，也会发送给sender.

      type=0 上线通知
          content=0，客户端通知服务器 客户上线
          content=1，客户端通知服务器 客服上线
          content=2，服务器通知客户端 已为客户分配客服；sender=客服；receiver=客户
          content=3，服务器通知客户的客户端 没有客服在线；receiver=客户

      type=1 普通消息
          content=消息内容，客户端应直接显示content

      type=2 离线通知，此时服务器不应该向sender发送信息，因为sender已离线
          content=0，服务器通知客服的客户端，有客户离线；sender=离线客户；receiver=接收的客服
          content=3，服务器通知客户的客户端，没有客服在线；sender=离线客服；receiver=客户

       */

import java.time.LocalDateTime;

public class Message {
    private String sender;
    private String receiver = null;
    private String content;
    private LocalDateTime sendTime;
    private int type;
    public final static int LOGIN = 0; // 上线通知
    public final static int NORMAL = 1; // 普通消息
    public final static int QUIT = 2; // 离线通知


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    // 构造方法
    Message(String content, String sender, String receiver, LocalDateTime sendTime, int type) {
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
        this.sendTime = sendTime;
        this.type = type;
    }
}
