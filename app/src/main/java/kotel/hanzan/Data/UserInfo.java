package kotel.hanzan.Data;

import java.util.HashMap;

public class UserInfo{
    public long id;
    public String name,profileImageAddress,personalCode;
    public int expireYYYY,expireMM,expireDD;
    public boolean isHanjanAvailableToday;
    public boolean finishedTutorial;

    public boolean justSignedUp = false;

    public UserInfo(HashMap<String,String> map){
        long id = Long.parseLong(map.get("id_member"));
        String name = map.get("name_member");
        String imageAddress = map.get("imgadd_member");
        String personalCode = map.get("personalcode");
        String membershipDue = map.get("membershipdue");
        boolean finishedTutorial = false;
        if(map.get("tutorialfinished_member")!=null && map.get("tutorialfinished_member").equals("TRUE")){
            finishedTutorial = true;
        }
        int year = Integer.parseInt(membershipDue.substring(0,4));
        int month = Integer.parseInt(membershipDue.substring(4,6));
        int day = Integer.parseInt(membershipDue.substring(6,8));
        boolean isAvailableToday = false;
        if(map.get("availabletoday")!=null && map.get("availabletoday").equals("TRUE")){
            isAvailableToday = true;
        }
        this.id = id;
        this.name = name;
        this.profileImageAddress = imageAddress;
        this.personalCode = personalCode;
        this.expireYYYY = year;
        this.expireMM = month;
        this.expireDD = day;
        this.isHanjanAvailableToday = isAvailableToday;
        this.finishedTutorial = finishedTutorial;
    }
}
