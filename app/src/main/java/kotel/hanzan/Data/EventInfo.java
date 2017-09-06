package kotel.hanzan.Data;

import java.io.Serializable;

public class EventInfo implements Serializable{
    public long id;
    public String titleImageAddress,mainImageAddress;
    public String title;

    public EventInfo(long id, String titleImageAddress, String mainImageAddress, String title) {
        this.id = id;
        this.titleImageAddress = titleImageAddress;
        this.mainImageAddress = mainImageAddress;
        this.title = title;
    }

}
