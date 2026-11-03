package impacthub.donation;

import impacthub.observer.ChangeEvent;
import impacthub.observer.Observer;
import impacthub.observer.Subject;

import java.util.ArrayList;
import java.util.List;

/** STUB - replace with teammate's real class. */
public class DonationService implements Subject {
    private final List<Observer> observers = new ArrayList<>();

    public Receipt buildReceipt(Donation donation, List<String> options) {
        return () -> "Receipt: " + donation.getAmount() + " from " + donation.getDonor().getName();
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(ChangeEvent change) {
        for (Observer o : observers) {
            o.update(this, change);
        }
    }
}
