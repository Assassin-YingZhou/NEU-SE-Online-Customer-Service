/**
 * 客户信息
 *
 * @author 舒意恒
 * @see Service
 */
public class Client {
    private String nickname;
    private Service myService;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Service getMyService() {
        return myService;
    }

    public void setMyService(Service myService) {
        this.myService = myService;
    }

    public Client(String nickname) {
        this.nickname = nickname;
    }
}
