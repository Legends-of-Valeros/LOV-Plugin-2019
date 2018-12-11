package com.legendsofvaleros.modules.factions;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;

@Table(name = "factions")
public class Faction {
    @Column(primary = true, name = "faction_id", length = 32)
    private String id;

    @Column(name = "faction_name", length = 32)
    protected String name;

    @Column(name = "faction_description")
    protected String description;

    @Column(name = "faction_rep_max")
    protected int maxReputation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxReputation() {
        return maxReputation;
    }

    public void setMaxReputation(int maxReputation) {
        this.maxReputation = maxReputation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}