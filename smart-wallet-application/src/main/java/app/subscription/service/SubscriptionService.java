package app.subscription.service;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.property.SubscriptionProperty;
import app.subscription.repository.SubscriptionRepository;
import app.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionProperty subscriptionProperty;
    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, SubscriptionProperty subscriptionProperty) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionProperty = subscriptionProperty;
    }

    public void createDefaultSubscription(User user) {
        Subscription subscription = subscriptionRepository.save(initializeSubscription(user));
        log.info("Successfully created default subscription with id [%s] and type [%s]"
                .formatted(subscription.getId(), subscription.getType()));

    }

    private Subscription initializeSubscription(User user) {

        LocalDateTime now=LocalDateTime.now();

        return Subscription.builder()
                .owner(user)
                .status(subscriptionProperty.getDefaultStatus())
                .period(subscriptionProperty.getDefaultPeriod())
                .type(subscriptionProperty.getDefaultType())
                .price(subscriptionProperty.getDefaultPrice())
                .renewalAllowed(subscriptionProperty.isDefaultRenewalAllowed())
                .createdOn(now)
                .completedOn(now.plusMonths(1))
                .build();
    }
}
