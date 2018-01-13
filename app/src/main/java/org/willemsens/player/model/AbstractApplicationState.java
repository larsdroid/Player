package org.willemsens.player.model;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

@Entity
abstract class AbstractApplicationState {
    @Key
    @Generated
    long id;

    @Column(unique = true, nullable = false)
    String property;

    @Column(nullable = false)
    String value;
}
