package kotel.hanzan.listener;


public interface TapBarItemClickListener {
    void onClick(String title, int number);

    void onClickStarted(String title, int number);
}
