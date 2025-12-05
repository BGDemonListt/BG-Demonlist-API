package com.bgdl.bgdl.services.impl.security.events;

import com.bgdl.bgdl.models.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * OnPasswordResetRequestEvent represents an event raised when a password reset request is initiated.
 * It carries information about the user requesting the password reset and the base URL of the application.
 */
@Getter
public class OnPasswordResetRequestEvent extends ApplicationEvent {
    private final User user;

    public OnPasswordResetRequestEvent(User user) {
        super(user);
        this.user = user;
    }
}