package impacthub;

/** MVC view: every screen can be shown and can display a message. */
public interface View {
    void show();

    void showMessage(String msg);
}
