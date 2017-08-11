package org.willemsens.player.model;

import java.util.Date;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

@Entity
abstract class AbstractDirectory {
    @Key
    @Generated
    long id;

    @Column(unique = true, nullable = false)
    String path;

    Date scanTimestamp;
}
