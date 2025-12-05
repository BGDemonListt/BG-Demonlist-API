package com.bgdl.bgdl.services.impl.security.events;

import com.bgdl.bgdl.models.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

/**
 * OnRegistrationCompleteEvent represents an event raised when a user completes registration.
 * It carries information about the registered user, the application URL, and the locale.
 */
@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    @Setter
    private User user;

    public OnRegistrationCompleteEvent(
            User user) {
        super(user);

        this.user = user;
    }
}
