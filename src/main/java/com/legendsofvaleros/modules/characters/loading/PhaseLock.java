package com.legendsofvaleros.modules.characters.loading;

import java.util.Objects;

/**
 * A lock that stops a phase from completing until it is released.
 */
public class PhaseLock {
    private final String name;

    public String getName() {
        return name;
    }

    private final Integer lockId;
    private final Callback<PhaseLock> callback;
    private volatile boolean locked;

    public PhaseLock(String name, int lockId, Callback<PhaseLock> callback) throws IllegalArgumentException {
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }

        this.name = name;
        this.lockId = lockId;
        this.callback = callback;
        this.locked = true;
    }

    /**
     * Relinquishes this lock and allows the phase to complete if it is not otherwise locked.
     */
    public void release() {
        locked = false;
        callback.callback(this, null);
    }

    /**
     * Gets the ID of this lock.
     * @return This lock's name.
     */
    public Integer getId() {
        return lockId;
    }

    /**
     * Gets whether this lock is currently active.
     * @return <code>true</code> if the lock is still active.
     */
    public boolean isLocked() {
        return locked;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + callback.hashCode();
        result = prime * result + lockId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PhaseLock other = (PhaseLock) obj;
        if (!callback.equals(other.callback))
            return false;
        if (!Objects.equals(lockId, other.lockId))
            return false;
        return true;
    }

}
