package kotel.hanzan.data;

import java.io.Serializable;

public class AnnouncementInfo implements Serializable{
    public int id;
    public String title,content,date;

    public AnnouncementInfo(int id, String title, String content, String date) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
    }
}