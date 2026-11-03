package impacthub.observer;

public interface Observer {
    void update(Subject source, ChangeEvent change);
}
