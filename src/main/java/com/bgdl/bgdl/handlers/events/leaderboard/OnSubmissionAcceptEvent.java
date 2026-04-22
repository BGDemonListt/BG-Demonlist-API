package com.bgdl.bgdl.handlers.events.leaderboard;

import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.Player;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OnSubmissionAcceptEvent extends ApplicationEvent {
    private final Player holder;
    private final Demon beatenDemon;

    public OnSubmissionAcceptEvent(Object source, Player holder, Demon beatenDemon) {
        super(source);
        this.holder = holder;
        this.beatenDemon = beatenDemon;
    }
}
