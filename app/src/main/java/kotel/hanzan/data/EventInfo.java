package kotel.hanzan.data;

import java.io.Serializable;

public class EventInfo implements Serializable{
    public long id;
    public String titleImageAddress,mainImageAddress;
    public String title,content,eventlink;

    public EventInfo(long id, String titleImageAddress, String mainImageAddress, String title, String content,String eventlink) {
        this.id = id;
        this.titleImageAddress = titleImageAddress;
        this.mainImageAddress = mainImageAddress;
        this.title = title;
        this.content = content;
        this.eventlink = eventlink;
    }

}
