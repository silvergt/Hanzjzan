package kotel.hanzan.Data;

import java.util.HashMap;

public class UserInfo{
    public long id;
    public String name,profileImageAddress,personalCode;
    public int expireYYYY,expireMM,expireDD;
    public boolean isHanzanAvailableToday;

    public UserInfo(){
        id=123;
        name="name";
        profileImageAddress = "https://cdn.pixabay.com/photo/2015/12/09/04/27/a-single-person-1084191_1280.jpg";
        expireYYYY = 2017;
        expireMM = 8;
        expireDD = 14;
        isHanzanAvailableToday = true;
    }

    public UserInfo(long id, String name, String profileImageAddress, String personalCode, int expireYYYY, int expireMM, int expireDD, boolean isHanzanAvailableToday) {
        this.id = id;
        this.name = name;
        this.profileImageAddress = profileImageAddress;
        this.personalCode = personalCode;
        this.expireYYYY = expireYYYY;
        this.expireMM = expireMM;
        this.expireDD = expireDD;
        this.isHanzanAvailableToday = isHanzanAvailableToday;
    }

    public UserInfo(HashMap<String,String> map){
        long id = Long.parseLong(map.get("id_member"));
        String name = map.get("name_member");
        String imageAddress = map.get("imgadd_member");
        String personalCode = map.get("personalcode");
        String membershipDue = map.get("membershipdue");
        int year = Integer.parseInt(membershipDue.substring(0,4));
        int month = Integer.parseInt(membershipDue.substring(4,6));
        int day = Integer.parseInt(membershipDue.substring(6,8));
        boolean isAvailableToday = false;
        if(map.get("availabletoday").equals("TRUE")){
            isAvailableToday = true;
        }
        this.id = id;
        this.name = name;
        this.profileImageAddress = imageAddress;
        this.personalCode = personalCode;
        this.expireYYYY = year;
        this.expireMM = month;
        this.expireDD = day;
        this.isHanzanAvailableToday = isAvailableToday;
    }
}
