package com.legendsofvaleros.modules.factions;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import lombok.Getter;

@Table(name = "factions")
public class Faction {
    @Column(primary = true, name = "faction_id", length = 32)
    private String id;

    public String getId() {
        return id;
    }

    @Column(name = "faction_name", length = 32)
    @Getter
    protected String name;

    @Column(name = "faction_description")
    @Getter
    protected String description;

    @Column(name = "faction_rep_max")
    @Getter
    protected int maxReputation;
}