package impacthub.observer;

public interface Subject {
    void addObserver(Observer o);

    void removeObserver(Observer o);

    void notifyObservers(ChangeEvent change);
}
