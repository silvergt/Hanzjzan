package kotel.hanzan.Data;

import com.nhn.android.maps.maplib.NGeoPoint;

public class StaticData {
    final public static String TESTAPI="sk_test_Q0kzLGdNWyQ0kzLGdNWy";


    /** These strings are used as prefix of new member's key, which is sended to server to perform sign in request.
     *  This makes sure that there won't be any repeated key among KAKAO id, FACEBOOK id, and other login account id. */
    final public static String IDENTIFIER_KAKAO="KAKAO";
    final public static String IDENTIFIER_FACEBOOK="FACEBOOK";

    /** Mobile device's width and height. can be used to make views in JAVA code.
     *  These variables can be used as a proportional number, but not as a absolute number
     *  since these numbers can be varied every time user restart the application.
     *  Nevertheless if you want to use these variables as a absolute number,
     *  you have to divide these numbers by DPI. */
    public static int displayHeight,displayWidth;

    /** Actual size of items without leftRightMargin on both sides. */
    public static int displayWidthWithoutMargin;

    /** currentUser is logged in UserInfo, transmitted from server.
     *  currentUser becomes null when system calls logout methods. */
    public static UserInfo currentUser;

    /** Holds observed coordinate every time when new coordinate observed.
     *  If GPS service suddenly become unavailable, system uses myLatestLocation as default location. */
    public static NGeoPoint myLatestLocation;

    /** Used when GPS service isn't available. Default loc : Shinchon Station (lng, lat order) */
    final public static NGeoPoint defaultLocation = new NGeoPoint(126.936862,37.555255);


    /** Default email address that receives message when user presses 'inquire' button. */
    final public static String adminEmail = "silvergt@naver.com";
}

