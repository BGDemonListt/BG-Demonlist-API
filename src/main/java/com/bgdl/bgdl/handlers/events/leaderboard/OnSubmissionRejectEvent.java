package com.bgdl.bgdl.handlers.events.leaderboard;

import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OnSubmissionRejectEvent extends ApplicationEvent {
    private final Player holder;
    private final Demon rejectedDemon;

    public OnSubmissionRejectEvent(Object source, Player holder, Demon rejectedDemon) {
        super(source);
        this.holder = holder;
        this.rejectedDemon = rejectedDemon;
    }
}
