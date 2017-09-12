package kotel.hanzan.listener;

public interface LocationFilterListener {
    void onItemClick(String locationName,boolean isActive);
    void onUpperBarItemClick(String locationName);
    void onSelectLocationClick(String locationName);
    void onSearchAroundMeClick();
}
