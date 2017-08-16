package kotel.hanzan.Data;

import java.io.Serializable;

public class UserInfo implements Serializable{
    public long id;
    public String name,nickname,profileImageAddress;
    public int expireYYYY,expireMM,expireDD;
    public boolean isHanzanAvailableToday;

    public UserInfo(){
        id=123;
        name="name";
        nickname="nickname";
        profileImageAddress = "https://cdn.pixabay.com/photo/2015/12/09/04/27/a-single-person-1084191_1280.jpg";
        expireYYYY = 2017;
        expireMM = 8;
        expireDD = 14;
        isHanzanAvailableToday = true;
    }
}
