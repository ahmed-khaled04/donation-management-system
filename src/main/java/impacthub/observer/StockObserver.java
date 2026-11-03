package impacthub.observer;

import impacthub.beneficiary.Distribution;
import impacthub.beneficiary.Resource;

public class StockObserver implements Observer {
    @Override
    public void update(Subject source, ChangeEvent change) {
        if (!"DISTRIBUTION_RECORDED".equals(change.getType())) {
            return;
        }
        Distribution distribution = (Distribution) change.getPayload();
        Resource resource = distribution.getResource();
        resource.setStockQuantity(resource.getStockQuantity() - distribution.getQuantity());
    }
}
