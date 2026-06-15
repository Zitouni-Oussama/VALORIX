package com.recyclix.backend.event;

import com.recyclix.backend.model.Collection;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CollectionCompletedEvent extends ApplicationEvent {

    private final Collection collection;

    public CollectionCompletedEvent(Object source, Collection collection) {
        super(source);
        this.collection = collection;
    }
}