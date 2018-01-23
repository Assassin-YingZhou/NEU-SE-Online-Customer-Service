/**
 * 客服信息
 *
 * @author 舒意恒
 * @see Client
 */

import java.util.ArrayList;

public class Service {
    private String nickname;
    private ArrayList<Client> myClients;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public ArrayList<Client> getMyClients() {
        return myClients;
    }

    public void setMyClients(ArrayList<Client> myClients) {
        this.myClients = myClients;
    }

    // 构造方法
    public Service(String nickname) {
        this.nickname = nickname;
        myClients = new ArrayList<>();
    }
}
