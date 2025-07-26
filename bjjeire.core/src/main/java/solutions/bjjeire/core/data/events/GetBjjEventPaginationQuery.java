package solutions.bjjeire.core.data.events;


import solutions.bjjeire.core.data.common.County;

/**
 * A specific query class for retrieving BJJ events with optional filters.
 * Corresponds to the C# GetBjjEventPaginationQuery.
 */
public class GetBjjEventPaginationQuery  {
    private County county;
    private BjjEventType type;

    public County getCounty() {
        return county;
    }

    public void setCounty(County county) {
        this.county = county;
    }

    public BjjEventType getType() {
        return type;
    }

    public void setType(BjjEventType type) {
        this.type = type;
    }
}